import { Router } from 'express'
import pool from '../db/database.js'
import { requireSuperOrga } from '../middleware/auth-superOrga.js'
import { parsePositiveInteger, parseInteger } from '../utils/validation.js'

const router = Router()

const TABLE_FIELDS = 'id, zone_du_plan_id, zone_tarifaire_id, capacite_jeux, nb_jeux_actuels, statut, created_at, updated_at'

// GET /api/tables
router.get('/', async (req, res) => {
    try {
        const params: unknown[] = []
        const clauses: string[] = []
        if (req.query.zoneDuPlanId) {
            const val = Number.parseInt(String(req.query.zoneDuPlanId), 10)
            if (!Number.isInteger(val)) {
                return res.status(400).json({ error: 'zoneDuPlanId doit être un entier' })
            }
            params.push(val)
            clauses.push(`zone_du_plan_id = $${params.length}`)
        }
        if (req.query.zoneTarifaireId) {
            const val = Number.parseInt(String(req.query.zoneTarifaireId), 10)
            if (!Number.isInteger(val)) {
                return res.status(400).json({ error: 'zoneTarifaireId doit être un entier' })
            }
            params.push(val)
            clauses.push(`zone_tarifaire_id = $${params.length}`)
        }
        if (req.query.statut) {
            const statut = String(req.query.statut)
            if (!['libre', 'reserve', 'plein', 'hors_service'].includes(statut)) {
                return res.status(400).json({ error: 'Statut invalide' })
            }
            params.push(statut)
            clauses.push(`statut = $${params.length}`)
        }
        const whereClause = clauses.length ? `WHERE ${clauses.join(' AND ')}` : ''
        const { rows } = await pool.query(
            `SELECT ${TABLE_FIELDS} FROM Table_Jeu ${whereClause} ORDER BY created_at DESC`,
            params
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des tables', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/tables/:id
router.get('/:id', async (req, res) => {
    const tableId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(tableId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `SELECT ${TABLE_FIELDS} FROM Table_Jeu WHERE id = $1`,
            [tableId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Table non trouvée' })
        }
        res.json(rows[0])
    } catch (err) {
        console.error(`Erreur lors de la lecture de la table ${tableId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/tables/:id/reservation
router.get('/:id/reservation', async (req, res) => {
    const tableId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(tableId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `SELECT rt.reservation_id, rt.table_id, rt.date_attribution, rt.attribue_par,
                    r.editeur_id, r.festival_id, r.statut_workflow
             FROM ReservationTable rt
             JOIN Reservation r ON rt.reservation_id = r.id
             WHERE rt.table_id = $1`,
            [tableId]
        )
        if (rows.length === 0) {
            return res.json(null)
        }
        res.json(rows[0])
    } catch (err) {
        console.error(`Erreur lors de la vérification de la réservation de la table ${tableId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/tables
router.post('/', requireSuperOrga, async (req, res) => {
    const client = await pool.connect()
    try {
        await client.query('BEGIN')
        
        const zoneDuPlanId = parsePositiveInteger(req.body?.zone_du_plan_id, 'zone_du_plan_id')
        const zoneTarifaireId = parsePositiveInteger(req.body?.zone_tarifaire_id, 'zone_tarifaire_id')
        const capacite = parsePositiveInteger(req.body?.capacite_jeux ?? 2, 'capacite_jeux')

        // Créer la table
        const { rows } = await client.query(
            `INSERT INTO Table_Jeu (zone_du_plan_id, zone_tarifaire_id, capacite_jeux)
            VALUES ($1, $2, $3)
            RETURNING ${TABLE_FIELDS}`,
            [zoneDuPlanId, zoneTarifaireId, capacite]
        )

        // Incrémenter nombre_tables de +1
        await client.query(
            `UPDATE ZoneDuPlan 
            SET nombre_tables = nombre_tables + 1
            WHERE id = $1`,
            [zoneDuPlanId]
        )

        await client.query('COMMIT')
        res.status(201).json({ message: 'Table créée', table: rows[0] })
    } catch (err: any) {
        await client.query('ROLLBACK')
        if (err.message?.startsWith('Le champ')) {
            return res.status(400).json({ error: err.message })
        }
        console.error('Erreur lors de la création de la table', err)
        res.status(500).json({ error: 'Erreur serveur' })
    } finally {
        client.release()
    }
})

// PUT /api/tables/:id
router.put('/:id', requireSuperOrga, async (req, res) => {
    const tableId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(tableId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const zoneDuPlanId = parsePositiveInteger(req.body?.zone_du_plan_id, 'zone_du_plan_id')
        const zoneTarifaireId = parsePositiveInteger(req.body?.zone_tarifaire_id, 'zone_tarifaire_id')
        const capacite = parsePositiveInteger(req.body?.capacite_jeux, 'capacite_jeux')
        const statut = req.body?.statut

        if (statut && !['libre', 'reserve', 'plein', 'hors_service'].includes(statut)) {
            return res.status(400).json({ error: 'Statut de table invalide' })
        }

        const { rows } = await pool.query(
            `UPDATE Table_Jeu
            SET zone_du_plan_id = $1,
                zone_tarifaire_id = $2,
                capacite_jeux = $3,
                statut = COALESCE($4, statut)
            WHERE id = $5
            RETURNING ${TABLE_FIELDS}`,
            [zoneDuPlanId, zoneTarifaireId, capacite, statut, tableId]
        )

        if (rows.length === 0) {
            return res.status(404).json({ error: 'Table non trouvée' })
        }

        res.json({ message: 'Table mise à jour', table: rows[0] })
    } catch (err: any) {
        if (err.message?.startsWith('Le champ')) {
            return res.status(400).json({ error: err.message })
        }
        console.error(`Erreur lors de la mise à jour de la table ${tableId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// DELETE /api/tables/:id
router.delete('/:id', requireSuperOrga, async (req, res) => {
    const tableId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(tableId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }

    const client = await pool.connect()
    try {
        await client.query('BEGIN')

        // Vérifier si la table est associée à une réservation
        const { rows: reservations } = await client.query(
            `SELECT 1 FROM ReservationTable WHERE table_id = $1 LIMIT 1`,
            [tableId]
        )
        if (reservations.length > 0) {
            await client.query('ROLLBACK')
            return res.status(400).json({ error: 'Impossible de supprimer : la table est associée à une réservation.' })
        }

        // Récupérer zone_du_plan_id avant suppression
        const { rows: tableRows } = await client.query(
            `SELECT zone_du_plan_id FROM Table_Jeu WHERE id = $1`,
            [tableId]
        )

        if (tableRows.length === 0) {
            await client.query('ROLLBACK')
            return res.status(404).json({ error: 'Table non trouvée' })
        }

        const zoneDuPlanId = tableRows[0].zone_du_plan_id

        // Supprimer la table
        const { rows } = await client.query(
            `DELETE FROM Table_Jeu WHERE id = $1 RETURNING id`,
            [tableId]
        )

        // Décrémenter nombre_tables de -1
        await client.query(
            `UPDATE ZoneDuPlan 
            SET nombre_tables = GREATEST(nombre_tables - 1, 0)
            WHERE id = $1`,
            [zoneDuPlanId]
        )

        await client.query('COMMIT')
        res.json({ message: 'Table supprimée', table: rows[0] })
    } catch (err) {
        await client.query('ROLLBACK')
        console.error(`Erreur lors de la suppression de la table ${tableId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    } finally {
        client.release()
    }
})

// GET /api/tables/:id/jeux
// Récupère tous les jeux associés à une table
router.get('/:id/jeux', async (req, res) => {
    const tableId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(tableId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `SELECT 
                j.id, 
                j.nom, 
                j.age_min, 
                j.age_max, 
                j.theme, 
                j.url_image, 
                tj.nom AS type_jeu_nom,
                STRING_AGG(DISTINCT e.nom, ', ') AS editeurs
            FROM JeuFestivalTable jft
            JOIN JeuFestival jf ON jf.id = jft.jeu_festival_id
            JOIN Jeu j ON j.id = jf.jeu_id
            LEFT JOIN TypeJeu tj ON tj.id = j.type_jeu_id
            LEFT JOIN JeuEditeur je ON je.jeu_id = j.id
            LEFT JOIN Editeur e ON e.id = je.editeur_id
            WHERE jft.table_id = $1
            GROUP BY j.id, j.nom,
                     j.age_min, j.age_max,  j.theme, 
                     j.url_image,  tj.nom
            ORDER BY j.nom`,
            [tableId]
        )
        res.json(rows)
    } catch (err) {
        console.error(`Erreur lors de la récupération des jeux de la table ${tableId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router
