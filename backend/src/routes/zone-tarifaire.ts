import { Router } from 'express'
import pool from '../db/database.js'
import { requireSuperOrga } from '../middleware/auth-superOrga.js'
import { parsePositiveInteger, sanitizeString, parseDecimal } from '../utils/validation.js'

const router = Router()
router.use(requireSuperOrga)
const FALLBACK_PRIX_M2_FACTOR = 4.5

let hasPrixM2ColumnPromise: Promise<boolean> | null = null

function ensurePrixM2ColumnPresence(): Promise<boolean> {
    if (!hasPrixM2ColumnPromise) {
        hasPrixM2ColumnPromise = pool.query(
            `SELECT 1 FROM information_schema.columns
             WHERE table_name = 'zonetarifaire' AND column_name = 'prix_m2'`
        )
            .then(result => (result.rowCount ?? 0) > 0)
            .catch(err => {
                console.warn('Impossible de vérifier la colonne prix_m2, utilisation du calcul fallback', err)
                return false
            })
    }
    return hasPrixM2ColumnPromise
}

function zoneTarifaireSelectFields(hasPrixM2: boolean) {
    return hasPrixM2
        ? 'id, festival_id, nom, nombre_tables_total, prix_table, prix_m2, created_at, updated_at'
        : `id, festival_id, nom, nombre_tables_total, prix_table, prix_table / ${FALLBACK_PRIX_M2_FACTOR} AS prix_m2, created_at, updated_at`
}

// GET /api/zones-tarifaires
router.get('/', async (req, res) => {
    try {
        const hasPrixM2 = await ensurePrixM2ColumnPresence()
        const filters: string[] = []
        const params: unknown[] = []
        if (req.query.festivalId) {
            filters.push('festival_id = $1')
            params.push(Number.parseInt(String(req.query.festivalId), 10))
        }
        const whereClause = filters.length ? `WHERE ${filters.join(' AND ')}` : ''
        const { rows } = await pool.query(
            `SELECT ${zoneTarifaireSelectFields(hasPrixM2)} FROM ZoneTarifaire ${whereClause} ORDER BY created_at DESC`,
            params
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des zones tarifaires', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/zones-tarifaires/:id
router.get('/:id', async (req, res) => {
    const zoneId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(zoneId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const hasPrixM2 = await ensurePrixM2ColumnPresence()
        const { rows } = await pool.query(
            `SELECT ${zoneTarifaireSelectFields(hasPrixM2)} FROM ZoneTarifaire WHERE id = $1`,
            [zoneId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Zone tarifaire non trouvée' })
        }
        res.json(rows[0])
    } catch (err) {
        console.error(`Erreur lors de la lecture de la zone tarifaire ${zoneId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/zones-tarifaires
router.post('/', async (req, res) => {
    try {
        const hasPrixM2 = await ensurePrixM2ColumnPresence()
        const festivalId = parsePositiveInteger(req.body?.festival_id, 'festival_id')
        const nom = sanitizeString(req.body?.nom)
        const nombreTables = parsePositiveInteger(req.body?.nombre_tables_total, 'nombre_tables_total')
        const prixTable = parseDecimal(req.body?.prix_table, 'prix_table')
        const prixM2 = req.body?.prix_m2 ? parseDecimal(req.body?.prix_m2, 'prix_m2') : prixTable / FALLBACK_PRIX_M2_FACTOR

        if (!nom) {
            return res.status(400).json({ error: 'Le nom de la zone est requis' })
        }

        const { rows } = hasPrixM2
            ? await pool.query(
                `INSERT INTO ZoneTarifaire (festival_id, nom, nombre_tables_total, prix_table, prix_m2)
                VALUES ($1, $2, $3, $4, $5)
                RETURNING ${zoneTarifaireSelectFields(true)}`,
                [festivalId, nom, nombreTables, prixTable, prixM2]
            )
            : await pool.query(
                `INSERT INTO ZoneTarifaire (festival_id, nom, nombre_tables_total, prix_table)
                VALUES ($1, $2, $3, $4)
                RETURNING ${zoneTarifaireSelectFields(false)}`,
                [festivalId, nom, nombreTables, prixTable]
            )
        res.status(201).json({ message: 'Zone tarifaire créée', zone: rows[0] })
    } catch (err: any) {
        if (err.message?.startsWith('Le champ')) {
            return res.status(400).json({ error: err.message })
        }
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Nom déjà utilisé pour ce festival' })
        }
        console.error('Erreur lors de la création de la zone tarifaire', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// PUT /api/zones-tarifaires/:id
router.put('/:id', async (req, res) => {
    const zoneId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(zoneId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const hasPrixM2 = await ensurePrixM2ColumnPresence()
        const nom = sanitizeString(req.body?.nom)
        const nombreTables = parsePositiveInteger(req.body?.nombre_tables_total, 'nombre_tables_total')
        const prixTable = parseDecimal(req.body?.prix_table, 'prix_table')
        const prixM2 = req.body?.prix_m2 ? parseDecimal(req.body?.prix_m2, 'prix_m2') : prixTable / FALLBACK_PRIX_M2_FACTOR

        if (!nom) {
            return res.status(400).json({ error: 'Le nom de la zone est requis' })
        }

        const { rows } = hasPrixM2
            ? await pool.query(
                `UPDATE ZoneTarifaire
                SET nom = $1,
                    nombre_tables_total = $2,
                    prix_table = $3,
                    prix_m2 = $4
                WHERE id = $5
                RETURNING ${zoneTarifaireSelectFields(true)}`,
                [nom, nombreTables, prixTable, prixM2, zoneId]
            )
            : await pool.query(
                `UPDATE ZoneTarifaire
                SET nom = $1,
                    nombre_tables_total = $2,
                    prix_table = $3
                WHERE id = $4
                RETURNING ${zoneTarifaireSelectFields(false)}`,
                [nom, nombreTables, prixTable, zoneId]
            )

        if (rows.length === 0) {
            return res.status(404).json({ error: 'Zone tarifaire non trouvée' })
        }

        res.json({ message: 'Zone tarifaire mise à jour', zone: rows[0] })
    } catch (err: any) {
        if (err.message?.startsWith('Le champ')) {
            return res.status(400).json({ error: err.message })
        }
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Nom déjà utilisé pour ce festival' })
        }
        console.error(`Erreur lors de la mise à jour de la zone ${zoneId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// DELETE /api/zones-tarifaires/:id
router.delete('/:id', async (req, res) => {
    const zoneId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(zoneId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }

    try {
        const { rows } = await pool.query(
            `DELETE FROM ZoneTarifaire WHERE id = $1 RETURNING id, nom`,
            [zoneId]
        )

        if (rows.length === 0) {
            return res.status(404).json({ error: 'Zone tarifaire non trouvée' })
        }

        res.json({ message: 'Zone tarifaire supprimée', zone: rows[0] })
    } catch (err) {
        console.error(`Erreur lors de la suppression de la zone ${zoneId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router
