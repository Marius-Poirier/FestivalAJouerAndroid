import { Router } from 'express'
import pool from '../db/database.js'
import { requireOrganisateur } from '../middleware/auth-organisateur.js'
import { sanitizeString } from '../utils/validation.js'

const router = Router()
const PERSON_FIELDS = 'id, nom, prenom, telephone, email, fonction, created_at, updated_at'

// GET /api/personnes?search=dupont
router.get('/', async (req, res) => {
    const search = typeof req.query?.search === 'string' ? sanitizeString(req.query.search) : null

    try {
        const filters: string[] = []
        const params: unknown[] = []

        if (search) {
            params.push(`%${search}%`)
            filters.push(`nom ILIKE $${params.length}`)
        }

        const whereClause = filters.length ? `WHERE ${filters.join(' AND ')}` : ''

        const { rows } = await pool.query(
            `SELECT ${PERSON_FIELDS} FROM Personne ${whereClause} ORDER BY nom ASC`,
            params
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des personnes', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/personnes/:id
router.get('/:id', async (req, res) => {
    const personId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(personId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `SELECT ${PERSON_FIELDS} FROM Personne WHERE id = $1`,
            [personId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Personne non trouvée' })
        }
        res.json(rows[0])
    } catch (err) {
        console.error(`Erreur lors de la récupération de la personne ${personId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/personnes
router.post('/', requireOrganisateur, async (req, res) => {
    const nom = sanitizeString(req.body?.nom)
    const prenom = sanitizeString(req.body?.prenom)
    const telephone = sanitizeString(req.body?.telephone)
    const email = sanitizeString(req.body?.email)
    const fonction = sanitizeString(req.body?.fonction)

    if (!nom || !prenom || !telephone) {
        return res.status(400).json({ error: 'nom, prenom et telephone sont requis' })
    }

    try {
        const { rows } = await pool.query(
            `INSERT INTO Personne (nom, prenom, telephone, email, fonction)
            VALUES ($1, $2, $3, NULLIF($4, ''), NULLIF($5, ''))
            RETURNING ${PERSON_FIELDS}`,
            [nom, prenom, telephone, email, fonction]
        )
        res.status(201).json({ message: 'Personne créée', personne: rows[0] })
    } catch (err: any) {
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Téléphone déjà utilisé' })
        }
        console.error('Erreur lors de la création de la personne', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// PUT /api/personnes/:id
router.put('/:id', requireOrganisateur, async (req, res) => {
    const personId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(personId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    const nom = sanitizeString(req.body?.nom)
    const prenom = sanitizeString(req.body?.prenom)
    const telephone = sanitizeString(req.body?.telephone)
    const email = sanitizeString(req.body?.email)
    const fonction = sanitizeString(req.body?.fonction)

    if (!nom || !prenom || !telephone) {
        return res.status(400).json({ error: 'nom, prenom et telephone sont requis' })
    }

    try {
        const { rows } = await pool.query(
            `UPDATE Personne
            SET nom = $1,
                prenom = $2,
                telephone = $3,
                email = NULLIF($4, ''),
                fonction = NULLIF($5, '')
            WHERE id = $6
            RETURNING ${PERSON_FIELDS}`,
            [nom, prenom, telephone, email, fonction, personId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Personne non trouvée' })
        }
        res.json({ message: 'Personne mise à jour', personne: rows[0] })
    } catch (err: any) {
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Téléphone déjà utilisé' })
        }
        console.error(`Erreur lors de la mise à jour de la personne ${personId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// DELETE /api/personnes/:id
router.delete('/:id', requireOrganisateur, async (req, res) => {
    const personId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(personId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `DELETE FROM Personne WHERE id = $1 RETURNING id, nom, prenom`,
            [personId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Personne non trouvée' })
        }
        res.json({ message: 'Personne supprimée', personne: rows[0] })
    } catch (err) {
        console.error(`Erreur lors de la suppression de la personne ${personId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router
