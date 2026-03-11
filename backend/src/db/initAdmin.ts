import 'dotenv/config'
import pool from './database.js'
import bcrypt from 'bcryptjs'

const DEFAULT_ADMIN_EMAIL = 'admin@festival.com'
const DEFAULT_ADMIN_PASSWORD = 'admin'

export async function ensureAdmin() {
    const email = process.env.ADMIN_EMAIL || DEFAULT_ADMIN_EMAIL
    const password = process.env.ADMIN_PASSWORD || DEFAULT_ADMIN_PASSWORD
    const hash = await bcrypt.hash(password, 10)

    await pool.query(
        `INSERT INTO Utilisateur (email, password_hash, role, statut, valide_par)
        VALUES ($1, $2, 'admin', 'valide', NULL)
        ON CONFLICT (email) DO UPDATE
            SET password_hash = EXCLUDED.password_hash,
                role = 'admin',
                statut = 'valide',
                email_bloque = FALSE,
                updated_at = CURRENT_TIMESTAMP`,
        [email, hash]
    )

    console.log(`Compte admin synchronisé pour ${email}`)
}