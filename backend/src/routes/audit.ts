import { Router } from 'express'
import pool from '../db/database.js'
import { requireAdmin } from '../middleware/auth-admin.js'

const router = Router()
router.use(requireAdmin)

const AUDIT_FIELDS = 'id, utilisateur_id, action, entite_type, entite_id, date_action, details'

// GET /api/audit
router.get('/', async (_req, res) => {
    try {
        const { rows } = await pool.query(
            `SELECT ${AUDIT_FIELDS} FROM AuditLog ORDER BY date_action DESC LIMIT 500`
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la lecture de l\'audit log', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router
