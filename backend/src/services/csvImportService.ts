import fs from 'fs';
import path from 'path';
import { parse } from 'csv-parse';
import pool from '../db/database.js';
import { fileURLToPath } from 'url';

const CSV_DIR = fileURLToPath(new URL('../../ressources/', import.meta.url));

/**
 * Generic helper to parse a CSV file and return rows
 */
function parseCsv(filePath: string): Promise<any[]> {
    return new Promise((resolve, reject) => {
        const results: any[] = [];
        if (!fs.existsSync(filePath)) {
            console.warn(`!! File not found: ${filePath}. Skipping.`);
            resolve([]);
            return;
        }
        fs.createReadStream(filePath)
            .pipe(parse({ columns: true, trim: true, skip_empty_lines: true })) 
            .on('data', (data) => results.push(data))
            .on('end', () => resolve(results))
            .on('error', (err) => reject(err));
    });
}

/**
 * Helper to clean integer values (handle empty strings)
 */
const toInt = (val: any) => (val && val.trim() !== '') ? parseInt(val, 10) : null;

/**
 * Helper to clean boolean values (CSV often has 0/1)
 */
const toBool = (val: any) => val === '1' || val === 'true';

/**
 * Helper to normalize optional strings (empty => null)
 */
const toNullableString = (val: any) => {
    if (typeof val !== 'string') return null;
    const trimmed = val.trim();
    return trimmed.length ? trimmed : null;
};

export async function importCsvData() {
    console.log("Starting CSV Data Import...");
    const client = await pool.connect();

    try {
        await client.query('BEGIN');

        // =========================================================
        // 1. IMPORT TYPES
        // =========================================================
        const types = await parseCsv(path.join(CSV_DIR, 'typeJeu.csv'));
        for (const row of types) {
            await client.query(
                `INSERT INTO TypeJeu (id, nom) VALUES ($1, $2)
                 ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom`,
                [toInt(row.idTypeJeu), row.libelleTypeJeu]
            );
        }

        // =========================================================
        // 2. IMPORT MECANISMES (With Duplicate Handling)
        // =========================================================
        const mecanisms = await parseCsv(path.join(CSV_DIR, 'mecanism.csv'));
        console.log(`Processing ${mecanisms.length} mechanisms...`);

        const mecaNameMap = new Map<string, number>();
        const mecaIdRemap = new Map<number, number>();

        for (const row of mecanisms) {
            const rawId = toInt(row.idMecanism);
            const name = row.mecaName;

            if (mecaNameMap.has(name)) {
                // DUPLICATE FOUND: Remap ID to the first one found (the "official" ID)
                const officialId = mecaNameMap.get(name)!;
                console.warn(`!! Duplicate Mechanism "${name}" found (ID ${rawId}). Remapping to ID ${officialId}.`);
                if (rawId !== null) mecaIdRemap.set(rawId, officialId);
            } else {
                // NEW MECHANISM
                if (rawId !== null) {
                    try {
                        await client.query(
                            `INSERT INTO Mecanisme (id, nom, description) VALUES ($1, $2, $3)
                             ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom, description = EXCLUDED.description`,
                            [rawId, name, row.mecaDesc]
                        );
                        mecaNameMap.set(name, rawId);
                    } catch (err: any) {
                        // Catching potential remaining DB errors (e.g., ID conflict)
                        console.error(`Error inserting mechanism ${rawId}:`, err.message);
                    }
                }
            }
        }

        // =========================================================
        // 3. IMPORT EDITEURS (With Duplicate & Validity Tracking)
        // =========================================================
        const editeurs = await parseCsv(path.join(CSV_DIR, 'editeur.csv'));
        console.log(`Processing ${editeurs.length} editors...`);

        const editorNameMap = new Map<string, number>();
        const editorIdRemap = new Map<number, number>();
        const validEditorIds = new Set<number>(); 

        for (const row of editeurs) {
            const rawId = toInt(row.idEditeur);
            const name = row.libelleEditeur;

            if (editorNameMap.has(name)) {
                // DUPLICATE FOUND: Remap ID to the first one found
                const officialId = editorNameMap.get(name)!;
                console.warn(`!! Duplicate Editor "${name}" found (ID ${rawId}). Remapping to ID ${officialId}.`);
                if (rawId !== null) editorIdRemap.set(rawId, officialId);
            } else {
                // NEW EDITOR
                if (rawId !== null) {
                    try {
                        await client.query(
                            `INSERT INTO Editeur (id, nom, logo_url) VALUES ($1, $2, $3)
                             ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom, logo_url = EXCLUDED.logo_url`,
                            [rawId, name, toNullableString(row.logoEditeur)]
                        );
                        editorNameMap.set(name, rawId);
                        validEditorIds.add(rawId); 
                    } catch (err: any) {
                        console.error(`Error inserting editor ${rawId}:`, err.message);
                    }
                }
            }
        }

        // =========================================================
        // 4. IMPORT JEUX (With Safe Editor Linking)
        // =========================================================
        const jeux = await parseCsv(path.join(CSV_DIR, 'jeu.csv'));
        console.log(`Processing ${jeux.length} games...`);

        for (const row of jeux) {
            const gameId = toInt(row.idJeu);
            
            await client.query(
                `INSERT INTO Jeu (
                    id, nom, nb_joueurs_min, nb_joueurs_max, duree_minutes, 
                    age_min, description, lien_regles, type_jeu_id, 
                    theme, url_image, url_video, prototype
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
                ON CONFLICT (id) DO UPDATE SET 
                    nom = EXCLUDED.nom,
                    nb_joueurs_min = EXCLUDED.nb_joueurs_min,
                    nb_joueurs_max = EXCLUDED.nb_joueurs_max,
                    duree_minutes = EXCLUDED.duree_minutes,
                    age_min = EXCLUDED.age_min,
                    description = EXCLUDED.description,
                    lien_regles = EXCLUDED.lien_regles,
                    type_jeu_id = EXCLUDED.type_jeu_id,
                    theme = EXCLUDED.theme,
                    url_image = EXCLUDED.url_image,
                    url_video = EXCLUDED.url_video,
                    prototype = EXCLUDED.prototype`,
                [
                    gameId, row.libelleJeu, toInt(row.nbMinJoueurJeu), toInt(row.nbMaxJoueurJeu),
                    toInt(row.duree), toInt(row.agemini), row.description, row.noticeJeu,
                    toInt(row.idTypeJeu), row.theme, row.imageJeu, row.videoRegle, toBool(row.prototype)
                ]
            );

            // Handle Editor Relation
            let editorId = toInt(row.idEditeur);
            
            // 1. Check Remap (if it was a duplicate)
            if (editorId !== null && editorIdRemap.has(editorId)) {
                editorId = editorIdRemap.get(editorId)!;
            }

            // 2. Check Validity (does the final ID exist in the DB? Handles missing IDs like 217)
            if (editorId !== null) {
                if (validEditorIds.has(editorId)) {
                    await client.query(
                        `INSERT INTO JeuEditeur (jeu_id, editeur_id) VALUES ($1, $2)
                         ON CONFLICT (jeu_id, editeur_id) DO NOTHING`,
                        [gameId, editorId]
                    );
                } else {
                    console.warn(`!! Skipped Editor Link: Game ${gameId} refers to missing Editor ${editorId}`);
                }
            }
        }

        // =========================================================
        // 5. IMPORT JEU_MECANISMES (With Safe Linking)
        // =========================================================
        const jeuMecas = await parseCsv(path.join(CSV_DIR, 'jeu_mecanism.csv'));
        console.log(`Processing ${jeuMecas.length} game-mechanism links...`);

        for (const row of jeuMecas) {
            const gameId = toInt(row.idJeu);
            let mecaId = toInt(row.idMecanism);

            // Check if this ID needs to be swapped (e.g. 51 -> 32)
            if (mecaId !== null && mecaIdRemap.has(mecaId)) {
                mecaId = mecaIdRemap.get(mecaId)!;
            }

            if (gameId && mecaId) {
                try {
                    await client.query(
                        `INSERT INTO JeuMecanisme (jeu_id, mecanisme_id) VALUES ($1, $2)
                         ON CONFLICT (jeu_id, mecanisme_id) DO NOTHING`,
                        [gameId, mecaId]
                    );
                } catch (err) {
                     // Silently ignore foreign key errors if the mecaId is entirely invalid/missing
                }
            }
        }

        await client.query('COMMIT');
        console.log("CSV Import completed successfully!");

    } catch (error) {
        await client.query('ROLLBACK');
        console.error("Error during CSV import:", error);
    } finally {
        client.release();
    }
}