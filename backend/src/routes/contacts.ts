import { Router } from 'express'
import pool from '../db/database.js'
import { requireOrganisateur } from '../middleware/auth-organisateur.js'
import { parsePositiveInteger, sanitizeString } from '../utils/validation.js'

const router = Router()
const CONTACT_FIELDS = 'id, editeur_id, festival_id, utilisateur_id, date_contact, notes'

// GET /api/contacts
router.get('/', async (req, res) => {
    try {
        const params: unknown[] = []
        const clauses: string[] = []
        if (req.query.festivalId) {
            const id = Number.parseInt(String(req.query.festivalId), 10)
            if (!Number.isInteger(id)) {
                return res.status(400).json({ error: 'festivalId invalide' })
            }
            params.push(id)
            clauses.push(`festival_id = $${params.length}`)
        }
        if (req.query.editeurId) {
            const id = Number.parseInt(String(req.query.editeurId), 10)
            if (!Number.isInteger(id)) {
                return res.status(400).json({ error: 'editeurId invalide' })
            }
            params.push(id)
            clauses.push(`editeur_id = $${params.length}`)
        }
        const whereClause = clauses.length ? `WHERE ${clauses.join(' AND ')}` : ''
        const { rows } = await pool.query(
            `SELECT ${CONTACT_FIELDS} FROM ContactEditeur ${whereClause} ORDER BY date_contact DESC`,
            params
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des contacts', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/contacts
router.post('/', requireOrganisateur, async (req, res) => {
    try {
        const editeurId = parsePositiveInteger(req.body?.editeur_id, 'editeur_id')
        const festivalId = parsePositiveInteger(req.body?.festival_id, 'festival_id')
        const notes = sanitizeString(req.body?.notes)
        const dateContact = req.body?.date_contact ?? new Date().toISOString()

        const { rows } = await pool.query(
            `INSERT INTO ContactEditeur (editeur_id, festival_id, utilisateur_id, date_contact, notes)
            VALUES ($1, $2, $3, $4, NULLIF($5, ''))
            RETURNING ${CONTACT_FIELDS}`,
            [editeurId, festivalId, req.user?.id, dateContact, notes]
        )
        res.status(201).json({ message: 'Contact enregistré', contact: rows[0] })
    } catch (err: any) {
        if (err.message?.includes('champ')) {
            return res.status(400).json({ error: err.message })
        }
        console.error('Erreur lors de la création du contact', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// DELETE /api/contacts/:id
router.delete('/:id', requireOrganisateur, async (req, res) => {
    const contactId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(contactId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `DELETE FROM ContactEditeur WHERE id = $1 RETURNING id`,
            [contactId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Contact non trouvé' })
        }
        res.json({ message: 'Contact supprimé', contact: rows[0] })
    } catch (err) {
        console.error(`Erreur lors de la suppression du contact ${contactId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router
