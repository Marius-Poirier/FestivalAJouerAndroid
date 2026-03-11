import { Router } from 'express'
import pool from '../db/database.js'
import { requireOrganisateur } from '../middleware/auth-organisateur.js'
import { parsePositiveInteger } from '../utils/validation.js'

const router = Router()
const FIELDS = `id, jeu_id, reservation_id, festival_id, dans_liste_demandee, dans_liste_obtenue, jeux_recu, created_at, updated_at`

// GET /api/jeu-festival/view?festivalId=...&reservationId=... (reservationId optionnel)
router.get('/view', async (req, res) => {
  try {
    const params: unknown[] = []
    const clauses: string[] = []

    // festivalId requis (workflow = festival scope)
    if (!req.query.festivalId) {
      return res.status(400).json({ error: 'festivalId est requis' })
    }
    const festivalId = Number.parseInt(String(req.query.festivalId), 10)
    if (!Number.isInteger(festivalId)) {
      return res.status(400).json({ error: 'festivalId invalide' })
    }
    params.push(festivalId)
    clauses.push(`jf.festival_id = $${params.length}`)

    // reservationId optionnel (si tu veux filtrer par réservation)
    if (req.query.reservationId) {
      const reservationId = Number.parseInt(String(req.query.reservationId), 10)
      if (!Number.isInteger(reservationId)) {
        return res.status(400).json({ error: 'reservationId invalide' })
      }
      params.push(reservationId)
      clauses.push(`jf.reservation_id = $${params.length}`)
    }

    const whereClause = clauses.length ? `WHERE ${clauses.join(' AND ')}` : ''

    const { rows } = await pool.query(
      `
      SELECT
        jf.id,
        jf.jeu_id,
        jf.reservation_id,
        jf.festival_id,
        jf.dans_liste_demandee,
        jf.dans_liste_obtenue,
        jf.jeux_recu,
        jf.created_at,
        jf.updated_at,

        j.nom AS jeu_nom,
        tj.nom AS type_jeu_nom,
        j.nb_joueurs_min,
        j.nb_joueurs_max,
        j.duree_minutes,
        j.age_min,
        j.age_max,
        j.theme,
        j.url_image,

        r.editeur_id,
        e.nom AS editeur_nom
      FROM JeuFestival jf
      JOIN Jeu j ON j.id = jf.jeu_id
      LEFT JOIN TypeJeu tj ON tj.id = j.type_jeu_id
      JOIN Reservation r ON r.id = jf.reservation_id
      JOIN Editeur e ON e.id = r.editeur_id
      ${whereClause}
      ORDER BY jf.updated_at DESC NULLS LAST, jf.created_at DESC
      `,
      params
    )

    res.json(rows)
  } catch (err) {
    console.error('Erreur lors de la récupération (view) des jeux festival', err)
    res.status(500).json({ error: 'Erreur serveur' })
  }
})

// GET /api/jeu-festival
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
        if (req.query.reservationId) {
            const id = Number.parseInt(String(req.query.reservationId), 10)
            if (!Number.isInteger(id)) {
                return res.status(400).json({ error: 'reservationId invalide' })
            }
            params.push(id)
            clauses.push(`reservation_id = $${params.length}`)
        }
        const whereClause = clauses.length ? `WHERE ${clauses.join(' AND ')}` : ''
        const { rows } = await pool.query(
            `SELECT ${FIELDS} FROM JeuFestival ${whereClause} ORDER BY updated_at DESC`,
            params
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des jeux festival', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/jeu-festival
router.post('/', requireOrganisateur, async (req, res) => {
    try {
        const jeuId = parsePositiveInteger(req.body?.jeu_id, 'jeu_id')
        const reservationId = parsePositiveInteger(req.body?.reservation_id, 'reservation_id')
        const festivalId = parsePositiveInteger(req.body?.festival_id, 'festival_id')
        const dansListeDemandee = Boolean(req.body?.dans_liste_demandee)
        const dansListeObtenue = Boolean(req.body?.dans_liste_obtenue)
        const jeuxRecu = Boolean(req.body?.jeux_recu)

        const { rows } = await pool.query(
            `INSERT INTO JeuFestival (jeu_id, reservation_id, festival_id, dans_liste_demandee, dans_liste_obtenue, jeux_recu)
            VALUES ($1, $2, $3, $4, $5, $6)
            RETURNING ${FIELDS}`,
            [jeuId, reservationId, festivalId, dansListeDemandee, dansListeObtenue, jeuxRecu]
        )
        res.status(201).json({ message: 'Jeu ajouté au festival', jeuFestival: rows[0] })
    } catch (err: any) {
        if (err.message?.includes('champ')) {
            return res.status(400).json({ error: err.message })
        }
        console.error('Erreur lors de l\'ajout du jeu festival', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// PUT /api/jeu-festival/:id
router.put('/:id', requireOrganisateur, async (req, res) => {
    const recordId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(recordId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const dansListeDemandee = req.body?.dans_liste_demandee !== undefined ? Boolean(req.body?.dans_liste_demandee) : undefined
        const dansListeObtenue = req.body?.dans_liste_obtenue !== undefined ? Boolean(req.body?.dans_liste_obtenue) : undefined
        const jeuxRecu = req.body?.jeux_recu !== undefined ? Boolean(req.body?.jeux_recu) : undefined

        const { rows } = await pool.query(
            `UPDATE JeuFestival
            SET dans_liste_demandee = COALESCE($1, dans_liste_demandee),
                dans_liste_obtenue = COALESCE($2, dans_liste_obtenue),
                jeux_recu = COALESCE($3, jeux_recu)
            WHERE id = $4
            RETURNING ${FIELDS}`,
            [dansListeDemandee, dansListeObtenue, jeuxRecu, recordId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Jeu festival non trouvé' })
        }
        res.json({ message: 'Jeu festival mis à jour', jeuFestival: rows[0] })
    } catch (err) {
        console.error(`Erreur lors de la mise à jour du jeu festival ${recordId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// DELETE /api/jeu-festival/:id
router.delete('/:id', requireOrganisateur, async (req, res) => {
    const recordId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(recordId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `DELETE FROM JeuFestival WHERE id = $1 RETURNING id`,
            [recordId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Jeu festival non trouvé' })
        }
        res.json({ message: 'Jeu festival supprimé', jeuFestival: rows[0] })
    } catch (err) {
        console.error(`Erreur lors de la suppression du jeu festival ${recordId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router
