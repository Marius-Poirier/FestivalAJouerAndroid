import { Router } from 'express'
import pool from '../db/database.js'
import { requireOrganisateur } from '../middleware/auth-organisateur.js'
import { sanitizeString, parsePositiveInteger, parseInteger } from '../utils/validation.js'

const router = Router()

// Updated to include new fields from your CSV structure
const GAME_FIELDS = `
    j.id, j.nom, j.nb_joueurs_min, j.nb_joueurs_max, j.duree_minutes, 
    j.age_min, j.age_max, j.description, j.lien_regles, 
    j.type_jeu_id, j.theme, j.url_image, j.url_video, j.prototype,
    j.created_at, j.updated_at
`

// GET /api/jeux?search=azul&sortBy=playersMin&sortOrder=desc
router.get('/', async (req, res) => {
    const search = typeof req.query?.search === 'string' ? sanitizeString(req.query.search) : null
    const sortByParam = typeof req.query?.sortBy === 'string' ? req.query.sortBy.toLowerCase() : 'name'
    const sortOrderParam = typeof req.query?.sortOrder === 'string' ? req.query.sortOrder.toLowerCase() : 'asc'

    const sortFields: Record<string, string> = {
        name: 'j.nom',
        playersmin: 'j.nb_joueurs_min',
        agemin: 'j.age_min'
    }
    const sortField = sortFields[sortByParam] ?? sortFields.name
    const sortDirection = sortOrderParam === 'desc' ? 'DESC' : 'ASC'

    try {
        const filters: string[] = []
        const params: unknown[] = []

        if (search) {
            params.push(`%${search}%`)
            filters.push(`j.nom ILIKE $${params.length}`)
        }

        const whereClause = filters.length ? `WHERE ${filters.join(' AND ')}` : ''

        // Left Join to get the Type name directly
        const { rows } = await pool.query(
            `SELECT ${GAME_FIELDS}, t.nom as type_jeu_nom
             FROM Jeu j
             LEFT JOIN TypeJeu t ON j.type_jeu_id = t.id
             ${whereClause}
             ORDER BY ${sortField} ${sortDirection}, j.nom ASC`,
            params
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des jeux', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/jeux/:id
router.get('/:id', async (req, res) => {
    const gameId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(gameId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        // Fetch basic game info
        const result = await pool.query(
            `SELECT ${GAME_FIELDS}, t.nom as type_jeu_nom
             FROM Jeu j
             LEFT JOIN TypeJeu t ON j.type_jeu_id = t.id
             WHERE j.id = $1`,
            [gameId]
        )

        if (result.rows.length === 0) {
            return res.status(404).json({ error: 'Jeu non trouvé' })
        }

        const game = result.rows[0]

        // Fetch associated Editors
        const editorsResult = await pool.query(
            `SELECT e.id, e.nom 
             FROM JeuEditeur je
             JOIN Editeur e ON je.editeur_id = e.id
             WHERE je.jeu_id = $1`,
            [gameId]
        )
        game.editeurs = editorsResult.rows

        // Fetch associated Mechanisms
        const mecasResult = await pool.query(
            `SELECT m.id, m.nom 
             FROM JeuMecanisme jm
             JOIN Mecanisme m ON jm.mecanisme_id = m.id
             WHERE jm.jeu_id = $1`,
            [gameId]
        )
        game.mecanismes = mecasResult.rows

        res.json(game)
    } catch (err) {
        console.error(`Erreur lors de la récupération du jeu ${gameId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/jeux
router.post('/', requireOrganisateur, async (req, res) => {
    const nom = sanitizeString(req.body?.nom)
    if (!nom) {
        return res.status(400).json({ error: 'Le nom est requis' })
    }

    const payload = {
        nbMin: req.body?.nb_joueurs_min ? parseInteger(req.body?.nb_joueurs_min, 'nb_joueurs_min') : null,
        nbMax: req.body?.nb_joueurs_max ? parseInteger(req.body?.nb_joueurs_max, 'nb_joueurs_max') : null,
        duree: req.body?.duree_minutes ? parsePositiveInteger(req.body?.duree_minutes, 'duree_minutes') : null,
        ageMin: req.body?.age_min ? parseInteger(req.body?.age_min, 'age_min') : null,
        ageMax: req.body?.age_max ? parseInteger(req.body?.age_max, 'age_max') : null,
        description: sanitizeString(req.body?.description),
        lienRegles: sanitizeString(req.body?.lien_regles),
        // New fields
        typeJeuId: req.body?.type_jeu_id ? parseInteger(req.body?.type_jeu_id, 'type_jeu_id') : null,
        theme: sanitizeString(req.body?.theme),
        urlImage: sanitizeString(req.body?.url_image),
        urlVideo: sanitizeString(req.body?.url_video),
        prototype: !!req.body?.prototype,
        // Relations (arrays of IDs)
        editeursIds: Array.isArray(req.body?.editeurs_ids) ? req.body.editeurs_ids : [],
        mecanismesIds: Array.isArray(req.body?.mecanismes_ids) ? req.body.mecanismes_ids : []
    }

    const client = await pool.connect()
    try {
        await client.query('BEGIN')

        // 1. Insert Game
        const { rows } = await client.query(
            `INSERT INTO Jeu (
                nom, nb_joueurs_min, nb_joueurs_max, duree_minutes, age_min, age_max, 
                description, lien_regles, type_jeu_id, theme, url_image, url_video, prototype
            )
            VALUES ($1, $2, $3, $4, $5, $6, NULLIF($7, ''), NULLIF($8, ''), $9, NULLIF($10, ''), NULLIF($11, ''), NULLIF($12, ''), $13)
            RETURNING id, nom`,
            [
                nom, payload.nbMin, payload.nbMax, payload.duree, payload.ageMin, payload.ageMax,
                payload.description, payload.lienRegles, payload.typeJeuId, payload.theme, 
                payload.urlImage, payload.urlVideo, payload.prototype
            ]
        )
        const newGameId = rows[0].id

        // 2. Insert Editors relations
        for (const editeurId of payload.editeursIds) {
            await client.query(
                `INSERT INTO JeuEditeur (jeu_id, editeur_id) VALUES ($1, $2)`, 
                [newGameId, editeurId]
            )
        }

        // 3. Insert Mechanisms relations
        for (const mecaId of payload.mecanismesIds) {
            await client.query(
                `INSERT INTO JeuMecanisme (jeu_id, mecanisme_id) VALUES ($1, $2)`, 
                [newGameId, mecaId]
            )
        }

        await client.query('COMMIT')
        res.status(201).json({ message: 'Jeu créé avec succès', jeu: rows[0] })
    } catch (err) {
        await client.query('ROLLBACK')
        console.error('Erreur lors de la création du jeu', err)
        res.status(500).json({ error: 'Erreur serveur' })
    } finally {
        client.release()
    }
})

// PUT /api/jeux/:id
router.put('/:id', requireOrganisateur, async (req, res) => {
    const gameId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(gameId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    const nom = sanitizeString(req.body?.nom)
    if (!nom) {
        return res.status(400).json({ error: 'Le nom est requis' })
    }

    const payload = {
        nbMin: req.body?.nb_joueurs_min ? parseInteger(req.body?.nb_joueurs_min, 'nb_joueurs_min') : null,
        nbMax: req.body?.nb_joueurs_max ? parseInteger(req.body?.nb_joueurs_max, 'nb_joueurs_max') : null,
        duree: req.body?.duree_minutes ? parsePositiveInteger(req.body?.duree_minutes, 'duree_minutes') : null,
        ageMin: req.body?.age_min ? parseInteger(req.body?.age_min, 'age_min') : null,
        ageMax: req.body?.age_max ? parseInteger(req.body?.age_max, 'age_max') : null,
        description: sanitizeString(req.body?.description),
        lienRegles: sanitizeString(req.body?.lien_regles),
        typeJeuId: req.body?.type_jeu_id ? parseInteger(req.body?.type_jeu_id, 'type_jeu_id') : null,
        theme: sanitizeString(req.body?.theme),
        urlImage: sanitizeString(req.body?.url_image),
        urlVideo: sanitizeString(req.body?.url_video),
        prototype: !!req.body?.prototype,
        editeursIds: Array.isArray(req.body?.editeurs_ids) ? req.body.editeurs_ids : null,
        mecanismesIds: Array.isArray(req.body?.mecanismes_ids) ? req.body.mecanismes_ids : null
    }

    const client = await pool.connect()
    try {
        await client.query('BEGIN')

        // 1. Update Game
        const { rows } = await client.query(
            `UPDATE Jeu
            SET nom = $1,
                nb_joueurs_min = $2,
                nb_joueurs_max = $3,
                duree_minutes = $4,
                age_min = $5,
                age_max = $6,
                description = NULLIF($7, ''),
                lien_regles = NULLIF($8, ''),
                type_jeu_id = $9,
                theme = NULLIF($10, ''),
                url_image = NULLIF($11, ''),
                url_video = NULLIF($12, ''),
                prototype = $13,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = $14
            RETURNING id, nom`,
            [
                nom, payload.nbMin, payload.nbMax, payload.duree, payload.ageMin, payload.ageMax,
                payload.description, payload.lienRegles, payload.typeJeuId, payload.theme,
                payload.urlImage, payload.urlVideo, payload.prototype, gameId
            ]
        )

        if (rows.length === 0) {
            await client.query('ROLLBACK')
            return res.status(404).json({ error: 'Jeu non trouvé' })
        }

        if (payload.editeursIds !== null) {
            await client.query('DELETE FROM JeuEditeur WHERE jeu_id = $1', [gameId])
            for (const editeurId of payload.editeursIds) {
                await client.query(
                    'INSERT INTO JeuEditeur (jeu_id, editeur_id) VALUES ($1, $2)', 
                    [gameId, editeurId]
                )
            }
        }

        if (payload.mecanismesIds !== null) {
            await client.query('DELETE FROM JeuMecanisme WHERE jeu_id = $1', [gameId])
            for (const mecaId of payload.mecanismesIds) {
                await client.query(
                    'INSERT INTO JeuMecanisme (jeu_id, mecanisme_id) VALUES ($1, $2)', 
                    [gameId, mecaId]
                )
            }
        }

        await client.query('COMMIT')
        res.json({ message: 'Jeu mis à jour', jeu: rows[0] })
    } catch (err) {
        await client.query('ROLLBACK')
        console.error(`Erreur lors de la mise à jour du jeu ${gameId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    } finally {
        client.release()
    }
})

// DELETE /api/jeux/:id
router.delete('/:id', requireOrganisateur, async (req, res) => {
    const gameId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(gameId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `DELETE FROM Jeu WHERE id = $1 RETURNING id, nom`,
            [gameId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Jeu non trouvé' })
        }
        res.json({ message: 'Jeu supprimé', jeu: rows[0] })
    } catch (err) {
        console.error(`Erreur lors de la suppression du jeu ${gameId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

export default router