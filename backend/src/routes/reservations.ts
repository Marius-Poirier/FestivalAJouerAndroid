import { Router } from 'express'
import pool from '../db/database.js'
import { requireOrganisateur } from '../middleware/auth-organisateur.js'
import { parsePositiveInteger, parseDecimal, sanitizeString } from '../utils/validation.js'

const router = Router()
const WORKFLOW_STATES = [
    'pas_contacte',
    'contact_pris',
    'discussion_en_cours',
    'sera_absent',
    'considere_absent',
    'present',
    'facture',
    'paiement_recu',
    'paiement_en_retard'
] as const

type Workflow = typeof WORKFLOW_STATES[number]

const RESERVATION_FIELDS = `id, editeur_id, festival_id, statut_workflow, editeur_presente_jeux,
remise_pourcentage, remise_montant, prix_total, prix_final, commentaires_paiement,
paiement_relance, date_facture, date_paiement, created_at, created_by, updated_at, updated_by`

function validateWorkflow(value: unknown): Workflow {
    if (typeof value !== 'string' || !WORKFLOW_STATES.includes(value as Workflow)) {
        throw new Error('Statut du workflow invalide')
    }
    return value as Workflow
}

// GET /api/reservations?search=102
router.get('/', async (req, res) => {
    try {
        const params: unknown[] = []
        const clauses: string[] = []
        const search = typeof req.query?.search === 'string' ? sanitizeString(req.query.search) : null

        if (search) {
            params.push(`%${search}%`)
            clauses.push(`CAST(id AS TEXT) ILIKE $${params.length}`)
        }
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
        if (req.query.statut) {
            if (!WORKFLOW_STATES.includes(String(req.query.statut) as Workflow)) {
                return res.status(400).json({ error: 'Statut invalide' })
            }
            params.push(req.query.statut)
            clauses.push(`statut_workflow = $${params.length}`)
        }
        const whereClause = clauses.length ? `WHERE ${clauses.join(' AND ')}` : ''
        const { rows } = await pool.query(
            `SELECT ${RESERVATION_FIELDS} FROM Reservation ${whereClause} ORDER BY updated_at DESC`,
            params
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des réservations', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/reservations/:id
router.get('/:id', async (req, res) => {
    const reservationId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(reservationId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `SELECT ${RESERVATION_FIELDS} FROM Reservation WHERE id = $1`,
            [reservationId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Réservation non trouvée' })
        }
        res.json(rows[0])
    } catch (err) {
        console.error(`Erreur lors de la récupération de la réservation ${reservationId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/reservations
router.post('/', requireOrganisateur, async (req, res) => {
    try {
        const editeurId = parsePositiveInteger(req.body?.editeur_id, 'editeur_id')
        const festivalId = parsePositiveInteger(req.body?.festival_id, 'festival_id')
        const statut = req.body?.statut_workflow ? validateWorkflow(req.body?.statut_workflow) : 'pas_contacte'
        const editeurPresente = Boolean(req.body?.editeur_presente_jeux)
        const remisePct = req.body?.remise_pourcentage ? parseDecimal(req.body?.remise_pourcentage, 'remise_pourcentage') : null
        const remiseMontant = req.body?.remise_montant ? parseDecimal(req.body?.remise_montant, 'remise_montant') : null
        const commentaires = sanitizeString(req.body?.commentaires_paiement)
        const paiementRelance = Boolean(req.body?.paiement_relance)
        const dateFacture = sanitizeString(req.body?.date_facture) || null
        const datePaiement = sanitizeString(req.body?.date_paiement) || null

        const { rows } = await pool.query(
            `INSERT INTO Reservation (editeur_id, festival_id, statut_workflow, editeur_presente_jeux,
            remise_pourcentage, remise_montant, commentaires_paiement, paiement_relance,
            date_facture, date_paiement, created_by, updated_by)
            VALUES ($1, $2, $3, $4, $5, $6, NULLIF($7, ''), $8, $9, $10, $11, $11)
            RETURNING ${RESERVATION_FIELDS}`,
            [
                editeurId,
                festivalId,
                statut,
                editeurPresente,
                remisePct,
                remiseMontant,
                commentaires,
                paiementRelance,
                dateFacture,
                datePaiement,
                req.user?.id ?? null
            ]
        )
        res.status(201).json({ message: 'Réservation créée', reservation: rows[0] })
    } catch (err: any) {
        if (err.message?.includes('champ')) {
            return res.status(400).json({ error: err.message })
        }
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Une réservation existe déjà pour cet éditeur et ce festival' })
        }
        console.error('Erreur lors de la création de la réservation', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// PUT /api/reservations/:id
// PUT /api/reservations/:id
router.put('/:id', requireOrganisateur, async (req, res) => {
    const reservationId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(reservationId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const statut = req.body?.statut_workflow ? validateWorkflow(req.body?.statut_workflow) : undefined
        const editeurPresente = req.body?.editeur_presente_jeux !== undefined ? Boolean(req.body?.editeur_presente_jeux) : undefined
        const remisePct = req.body?.remise_pourcentage !== undefined ? parseDecimal(req.body?.remise_pourcentage, 'remise_pourcentage') : undefined
        const remiseMontant = req.body?.remise_montant !== undefined ? parseDecimal(req.body?.remise_montant, 'remise_montant') : undefined
        const commentaires = req.body?.commentaires_paiement !== undefined ? sanitizeString(req.body?.commentaires_paiement) : undefined
        const paiementRelance = req.body?.paiement_relance !== undefined ? Boolean(req.body?.paiement_relance) : undefined
        const dateFacture = req.body?.date_facture !== undefined ? sanitizeString(req.body?.date_facture) : undefined
        const datePaiement = req.body?.date_paiement !== undefined ? sanitizeString(req.body?.date_paiement) : undefined

        const { rows } = await pool.query(
            `UPDATE Reservation
            SET statut_workflow = COALESCE($1, statut_workflow),
                editeur_presente_jeux = COALESCE($2, editeur_presente_jeux),
                remise_pourcentage = COALESCE($3, remise_pourcentage),
                remise_montant = COALESCE($4, remise_montant),
                commentaires_paiement = COALESCE($5, commentaires_paiement),
                paiement_relance = COALESCE($6, paiement_relance),
                date_facture = COALESCE($7, date_facture),
                date_paiement = COALESCE($8, date_paiement),
                updated_by = $9
            WHERE id = $10
            RETURNING ${RESERVATION_FIELDS}`,
            [
                statut,
                editeurPresente,
                remisePct,
                remiseMontant,
                commentaires,
                paiementRelance,
                dateFacture,
                datePaiement,
                req.user?.id ?? null,
                reservationId
            ]
        )

        if (rows.length === 0) {
            return res.status(404).json({ error: 'Réservation non trouvée' })
        }

        res.json({ message: 'Réservation mise à jour', reservation: rows[0] })
    } catch (err: any) {
        if (err.message === 'Statut du workflow invalide' || err.message?.includes('champ')) {
            return res.status(400).json({ error: err.message })
        }
        console.error(`Erreur lors de la mise à jour de la réservation ${reservationId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// DELETE /api/reservations/:id
router.delete('/:id', requireOrganisateur, async (req, res) => {
    const reservationId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(reservationId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `DELETE FROM Reservation WHERE id = $1 RETURNING id`,
            [reservationId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Réservation non trouvée' })
        }
        res.json({ message: 'Réservation supprimée', reservation: rows[0] })
    } catch (err) {
        console.error(`Erreur lors de la suppression de la réservation ${reservationId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router
