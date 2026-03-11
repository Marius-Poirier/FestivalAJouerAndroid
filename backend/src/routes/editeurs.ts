import { Router } from 'express'
import pool from '../db/database.js'
import { requireOrganisateur } from '../middleware/auth-organisateur.js'
import { sanitizeString } from '../utils/validation.js'
import { requireSuperOrga } from '../middleware/auth-superOrga.js'

const router = Router()
const EDITOR_FIELDS = 'id, nom, logo_url, created_at, updated_at'

// GET /api/editeurs?search=ludo
router.get('/', async (req, res) => {
    const search = typeof req.query?.search === 'string' ? sanitizeString(req.query.search) : null

    try {
        const filters: string[] = []
        const params: unknown[] = []

        if (search) {
            params.push(`%${search}%`)
            filters.push(`nom ILIKE $${params.length}`)
        }
          if (req.query?.festivalId !== undefined) {
            const id = Number.parseInt(String(req.query.festivalId), 10)
            if (!Number.isInteger(id)) {
                return res.status(400).json({ error: 'festivalId invalide' })
            }
            params.push(id)
            // CHANGE 1: Use 'e.id' (alias) instead of 'Editeur.id'
            filters.push(
                `EXISTS (SELECT 1 FROM Reservation r WHERE r.editeur_id = e.id AND r.festival_id = $${params.length})`
            )
        }

        const whereClause = filters.length ? `WHERE ${filters.join(' AND ')}` : ''

        const { rows } = await pool.query(
            `SELECT ${EDITOR_FIELDS} FROM Editeur e ${whereClause} ORDER BY nom ASC`,
            params
        )
        res.json(rows)
    } catch (err) {
        console.error('Erreur lors de la récupération des éditeurs', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/editeurs/:id
router.get('/:id', async (req, res) => {
    const editorId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(editorId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `SELECT ${EDITOR_FIELDS} FROM Editeur WHERE id = $1`,
            [editorId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Éditeur non trouvé' })
        }
        res.json(rows[0])
    } catch (err) {
        console.error(`Erreur lors de la récupération de l'éditeur ${editorId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/editeurs
router.post('/', requireOrganisateur, async (req, res) => {
    const nom = sanitizeString(req.body?.nom)
    const logoInput = sanitizeString(req.body?.logoUrl ?? req.body?.logo_url)
    const logoUrl = logoInput || null
    if (!nom) {
        return res.status(400).json({ error: 'Le nom est requis' })
    }
    try {
        const { rows } = await pool.query(
            `INSERT INTO Editeur (nom, logo_url) VALUES ($1, $2)
            RETURNING ${EDITOR_FIELDS}`,
            [nom, logoUrl]
        )
        res.status(201).json({ message: 'Éditeur créé', editeur: rows[0] })
    } catch (err: any) {
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Ce nom d\'éditeur existe déjà' })
        }
        console.error('Erreur lors de la création de l\'éditeur', err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// PUT /api/editeurs/:id
router.put('/:id', requireOrganisateur, async (req, res) => {
    const editorId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(editorId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    const nom = sanitizeString(req.body?.nom)
    const logoInput = sanitizeString(req.body?.logoUrl ?? req.body?.logo_url)
    const logoUrl = logoInput || null
    if (!nom) {
        return res.status(400).json({ error: 'Le nom est requis' })
    }
    try {
        const { rows } = await pool.query(
            `UPDATE Editeur SET nom = $1, logo_url = $2, updated_at = CURRENT_TIMESTAMP WHERE id = $3 RETURNING ${EDITOR_FIELDS}`,
            [nom, logoUrl, editorId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Éditeur non trouvé' })
        }
        res.json({ message: 'Éditeur mis à jour', editeur: rows[0] })
    } catch (err: any) {
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Ce nom d\'éditeur existe déjà' })
        }
        console.error(`Erreur lors de la mise à jour de l\'éditeur ${editorId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// DELETE /api/editeurs/:id
router.delete('/:id', requireOrganisateur, async (req, res) => {
    const editorId = Number.parseInt(req.params.id, 10)
    if (!Number.isInteger(editorId)) {
        return res.status(400).json({ error: 'Identifiant invalide' })
    }
    try {
        const { rows } = await pool.query(
            `DELETE FROM Editeur WHERE id = $1 RETURNING id, nom`,
            [editorId]
        )
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Éditeur non trouvé' })
        }
        res.json({ message: 'Éditeur supprimé', editeur: rows[0] })
    } catch (err) {
        console.error(`Erreur lors de la suppression de l\'éditeur ${editorId}`, err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// GET /api/editeurs/:id/jeux
router.get('/:id/jeux', async (req, res) => {
  const editorId = Number.parseInt(req.params.id, 10);
  if (!Number.isInteger(editorId)) {
    return res.status(400).json({ error: 'Identifiant invalide' });
  }

  try {
    const { rows } = await pool.query(
      `SELECT j.id, j.nom, j.nb_joueurs_min, j.nb_joueurs_max, j.duree_minutes, j.age_min, j.age_max, j.description, j.lien_regles, j.created_at, j.updated_at
       FROM Jeu j
       JOIN JeuEditeur je ON je.jeu_id = j.id
       WHERE je.editeur_id = $1
       ORDER BY j.nom ASC`,
      [editorId]
    );

    res.json(rows);
  } catch (err) {
    console.error(
      `Erreur lors de la récupération des jeux pour l'éditeur ${editorId}`,
      err
    );
    res.status(500).json({ error: 'Erreur serveur' });
  }
})

// GET /api/editeurs/:id/personnes
router.get('/:id/personnes', requireOrganisateur, async (req, res) => {
  const editorId = Number.parseInt(req.params.id, 10);
  if (!Number.isInteger(editorId)) {
    return res.status(400).json({ error: 'Identifiant invalide' });
  }

  try {
    const { rows } = await pool.query(
      `SELECT p.id, p.nom, p.prenom, p.telephone, p.email, p.fonction, p.created_at, p.updated_at
       FROM Personne p
       JOIN EditeurContact ec ON ec.personne_id = p.id
       WHERE ec.editeur_id = $1
       ORDER BY p.nom ASC, p.prenom ASC`,
      [editorId]
    );

    res.json(rows);
  } catch (err) {
    console.error(`Erreur lors de la récupération des contacts pour l'éditeur ${editorId}`, err);
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

// POST /api/editeurs/:id/personnes
router.post('/:id/personnes', requireOrganisateur, async (req, res) => {
  const editorId = Number.parseInt(req.params.id, 10);
  if (!Number.isInteger(editorId)) {
    return res.status(400).json({ error: 'Identifiant éditeur invalide' });
  }

  const nom = sanitizeString(req.body?.nom);
  const prenom = sanitizeString(req.body?.prenom);
  const telephone = sanitizeString(req.body?.telephone);
  const email = sanitizeString(req.body?.email);
  const fonction = sanitizeString(req.body?.fonction);

  if (!nom || !prenom || !telephone) {
    return res.status(400).json({ error: 'nom, prenom et telephone sont requis' });
  }

  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    // 1) récup ou création de la personne via téléphone unique
    const existing = await client.query(
      `SELECT id FROM Personne WHERE telephone = $1`,
      [telephone]
    );

    let personneId;

    if (existing.rows.length) {
      personneId = existing.rows[0].id;

      // On met à jour les champs
      await client.query(
        `UPDATE Personne
         SET nom = $1,
             prenom = $2,
             email = NULLIF($3, ''),
             fonction = NULLIF($4, ''),
             updated_at = CURRENT_TIMESTAMP
         WHERE id = $5`,
        [nom, prenom, email ?? '', fonction ?? '', personneId]
      );
    } else {
      const inserted = await client.query(
        `INSERT INTO Personne (nom, prenom, telephone, email, fonction)
         VALUES ($1, $2, $3, NULLIF($4, ''), NULLIF($5, ''))
         RETURNING id`,
        [nom, prenom, telephone, email ?? '', fonction ?? '']
      );
      personneId = inserted.rows[0].id;
    }

    // 2) lien éditeur-personne
    await client.query(
      `INSERT INTO EditeurContact (editeur_id, personne_id)
       VALUES ($1, $2)
       ON CONFLICT DO NOTHING`,
      [editorId, personneId]
    );

    // 3) renvoyer la personne complète
    const { rows } = await client.query(
      `SELECT id, nom, prenom, telephone, email, fonction, created_at, updated_at
       FROM Personne
       WHERE id = $1`,
      [personneId]
    );

    await client.query('COMMIT');
    res.status(201).json({ message: 'Contact ajouté', personne: rows[0] });
  } catch (err) {
    await client.query('ROLLBACK');
    console.error('Erreur ajout contact éditeur', err);
    res.status(500).json({ error: 'Erreur serveur' });
  } finally {
    client.release();
  }
});

// DELETE /api/editeurs/:id/personnes/:personneId
router.delete('/:id/personnes/:personneId', requireSuperOrga, async (req, res) => {
  const editorId = Number.parseInt(req.params.id, 10);
  const personneId = Number.parseInt(req.params.personneId, 10);

  if (!Number.isInteger(editorId) || !Number.isInteger(personneId)) {
    return res.status(400).json({ error: 'Identifiant invalide' });
  }

  try {
    const { rows } = await pool.query(
      `DELETE FROM EditeurContact
       WHERE editeur_id = $1 AND personne_id = $2
       RETURNING editeur_id, personne_id`,
      [editorId, personneId]
    );

    if (!rows.length) {
      return res.status(404).json({ error: 'Lien contact introuvable' });
    }

    res.json({ message: 'Contact retiré', link: rows[0] });
  } catch (err) {
    console.error('Erreur suppression lien EditeurContact', err);
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

export default router
