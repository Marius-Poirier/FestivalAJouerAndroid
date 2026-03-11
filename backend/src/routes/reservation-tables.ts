import { Router } from 'express'
import pool from '../db/database.js'
import { requireOrganisateur } from '../middleware/auth-organisateur.js'
import { parsePositiveInteger } from '../utils/validation.js'

const router = Router()
const FIELDS = 'reservation_id, table_id, date_attribution, attribue_par'

// GET /api/reservation-tables
router.get('/', async (req, res) => {
    if (!req.query.reservationId) {
        return res.status(400).json({ error: 'reservationId est requis' })
    }
    const reservationId = Number.parseInt(String(req.query.reservationId), 10)
    if (!Number.isInteger(reservationId)) {
        return res.status(400).json({ error: 'reservationId invalide' })
    }
    try {
        const { rows } = await pool.query(
            `SELECT ${FIELDS} FROM ReservationTable WHERE reservation_id = $1 ORDER BY date_attribution DESC`,
            [reservationId]
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des tables de réservation', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/reservation-tables
router.post('/', requireOrganisateur, async (req, res) => {
    try {
        const reservationId = parsePositiveInteger(req.body?.reservation_id, 'reservation_id')
        const tableId = parsePositiveInteger(req.body?.table_id, 'table_id')
        const { rows } = await pool.query(
            `INSERT INTO ReservationTable (reservation_id, table_id, attribue_par)
            VALUES ($1, $2, $3)
            RETURNING ${FIELDS}`,
            [reservationId, tableId, req.user?.id ?? null]
        )
        res.status(201).json({ message: 'Table attribuée', affectation: rows[0] })
    } catch (err: any) {
        if (err.message?.includes('champ')) {
            return res.status(400).json({ error: err.message })
        }
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Cette table est déjà attribuée à cette réservation' })
        }
        console.error('Erreur lors de l\'attribution de la table', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// DELETE /api/reservation-tables
router.delete('/', requireOrganisateur, async (req, res) => {
    const client = await pool.connect()
    try {
        const reservationId = parsePositiveInteger(req.body?.reservation_id, 'reservation_id')
        const tableId = parsePositiveInteger(req.body?.table_id, 'table_id')
        await client.query('BEGIN')
        const { rows } = await client.query(
            `DELETE FROM ReservationTable 
             WHERE reservation_id = $1 AND table_id = $2 
             RETURNING ${FIELDS}`,
            [reservationId, tableId]
        )
        if (rows.length === 0) {
            await client.query('ROLLBACK')
            return res.status(404).json({ error: 'Affectation non trouvée' })
        }
        await client.query(
            `
            DELETE FROM JeuFestivalTable jft
            USING JeuFestival jf
            WHERE jft.jeu_festival_id = jf.id
              AND jf.reservation_id = $1
              AND jft.table_id = $2
            `,
            [reservationId, tableId]
        )

        await client.query('COMMIT')
        res.json({ message: 'Affectation supprimée', affectation: rows[0] })
    } catch (err: any) {
        await client.query('ROLLBACK')
        if (err.message?.includes('champ')) {
            return res.status(400).json({ error: err.message })
        }
        console.error('Erreur lors de la suppression de l\'affectation', err)
        res.status(500).json({ error: 'Erreur serveur' })
    } finally {
        client.release()
    }
})

export default router
