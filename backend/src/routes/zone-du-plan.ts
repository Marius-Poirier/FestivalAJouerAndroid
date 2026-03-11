import { Router } from 'express'
import pool from '../db/database.js'
import { requireSuperOrga } from '../middleware/auth-superOrga.js'
import { parsePositiveInteger, sanitizeString, parseInteger } from '../utils/validation.js'

const router = Router()

const ZONE_PLAN_FIELDS = 'id, festival_id, nom, nombre_tables, zone_tarifaire_id, created_at, updated_at'

// GET /api/zones-du-plan
router.get('/', async (req, res) => {
    try {
        const params: unknown[] = []
        let whereClause = ''
        if (req.query.festivalId) {
            const festivalId = Number.parseInt(String(req.query.festivalId), 10)
            if (!Number.isInteger(festivalId)) {
                return res.status(400).json({ error: 'festivalId doit être un entier' })
            }
            params.push(festivalId)
            whereClause = 'WHERE festival_id = $1'
        }
        const { rows } = await pool.query(
            `SELECT ${ZONE_PLAN_FIELDS} FROM ZoneDuPlan ${whereClause} ORDER BY created_at DESC`,
            params
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des zones du plan', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/zones-du-plan/:id
router.get('/:id', async (req, res) => {
    const zoneId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(zoneId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `SELECT ${ZONE_PLAN_FIELDS} FROM ZoneDuPlan WHERE id = $1`,
            [zoneId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Zone du plan non trouvée' })
        }
        res.json(rows[0])
    } catch (err) {
        console.error(`Erreur lors de la lecture de la zone du plan ${zoneId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/zones-du-plan
router.post('/', requireSuperOrga, async (req, res) => {
    try {
        const festivalId = parsePositiveInteger(req.body?.festival_id, 'festival_id')
        const zoneTarifaireId = parsePositiveInteger(req.body?.zone_tarifaire_id, 'zone_tarifaire_id')
        const nom = sanitizeString(req.body?.nom)
        // nombre_tables est initialisé à 0 à la création, puis
        // incrémenté/décrémenté par les routes de tables (add/delete)

        if (!nom) {
            return res.status(400).json({ error: 'Le nom de la zone est requis' })
        }

        const { rows } = await pool.query(
            `INSERT INTO ZoneDuPlan (festival_id, nom, nombre_tables, zone_tarifaire_id)
            VALUES ($1, $2, 0, $3)
            RETURNING ${ZONE_PLAN_FIELDS}`,
            [festivalId, nom, zoneTarifaireId]
        )

        res.status(201).json({ message: 'Zone du plan créée', zone: rows[0] })
    } catch (err: any) {
        if (err.message?.startsWith('Le champ')) {
            return res.status(400).json({ error: err.message })
        }
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Nom déjà utilisé pour ce festival' })
        }
        console.error('Erreur lors de la création de la zone du plan', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// PUT /api/zones-du-plan/:id
router.put('/:id', requireSuperOrga, async (req, res) => {
    const zoneId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(zoneId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }

    try {
        const nom = sanitizeString(req.body?.nom)
        const nombreTables = parsePositiveInteger(req.body?.nombre_tables, 'nombre_tables')
        const zoneTarifaireId = parsePositiveInteger(req.body?.zone_tarifaire_id, 'zone_tarifaire_id')

        if (!nom) {
            return res.status(400).json({ error: 'Le nom de la zone est requis' })
        }

        const { rows } = await pool.query(
            `UPDATE ZoneDuPlan
            SET nom = $1,
                nombre_tables = $2,
                zone_tarifaire_id = $3
            WHERE id = $4
            RETURNING ${ZONE_PLAN_FIELDS}`,
            [nom, nombreTables, zoneTarifaireId, zoneId]
        )

        if (rows.length === 0) {
            return res.status(404).json({ error: 'Zone du plan non trouvée' })
        }

        res.json({ message: 'Zone du plan mise à jour', zone: rows[0] })
    } catch (err: any) {
        if (err.message?.startsWith('Le champ')) {
            return res.status(400).json({ error: err.message })
        }
        console.error(`Erreur lors de la mise à jour de la zone du plan ${zoneId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// DELETE /api/zones-du-plan/:id
router.delete('/:id', requireSuperOrga, async (req, res) => {
    const zoneId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(zoneId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }

    try {
        const { rows } = await pool.query(
            `DELETE FROM ZoneDuPlan WHERE id = $1 RETURNING id, nom`,
            [zoneId]
        )

        if (rows.length === 0) {
            return res.status(404).json({ error: 'Zone du plan non trouvée' })
        }

        res.json({ message: 'Zone du plan supprimée', zone: rows[0] })
    } catch (err) {
        console.error(`Erreur lors de la suppression de la zone du plan ${zoneId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router
