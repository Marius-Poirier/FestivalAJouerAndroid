import { Router } from 'express'
import pool from '../db/database.js'

const router = Router()

// GET /api/metadata/types-jeu
router.get('/types-jeu', async (_req, res) => {
    try {
        const { rows } = await pool.query(
            `SELECT id, nom FROM TypeJeu ORDER BY nom ASC`
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des types de jeu', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/metadata/mecanismes
router.get('/mecanismes', async (_req, res) => {
    try {
        const { rows } = await pool.query(
            `SELECT id, nom, description FROM Mecanisme ORDER BY nom ASC`
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des mécanismes', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router