import { Router } from 'express'
import jwt from 'jsonwebtoken'
import bcrypt from 'bcryptjs'
import pool from '../db/database.js'
import { verifyToken, createAccessToken, createRefreshToken } from '../middleware/token-management.js';
import type { TokenPayload } from '../types/token-payload.js';

const router = Router()

// POST /api/auth/login
router.post('/login', async (req, res) => {
    const { email, password } = req.body
    if (!email || !password) {
        return res.status(400).json({ error: 'Email et mot de passe manquants' })
    }
    
    // Récupérer l'utilisateur
    const { rows } = await pool.query(
        'SELECT * FROM Utilisateur WHERE email=$1',
        [email]
    )
    const user = rows[0]
    
    if (!user) {
        return res.status(401).json({ error: 'Utilisateur inconnu' })
    }
    
    // Vérifier le statut du compte
    if (user.statut === 'en_attente') {
        return res.status(403).json({ error: 'Votre compte est en attente de validation' })
    }
    
    if (user.statut === 'refuse') {
        return res.status(403).json({ error: 'Votre demande de compte a été refusée' })
    }
    
    if (user.email_bloque) {
        return res.status(403).json({ error: 'Votre compte a été bloqué' })
    }
    
    // Vérifier le mot de passe
    const match = await bcrypt.compare(password, user.password_hash)
    if (!match) {
        return res.status(401).json({ error: 'Mot de passe incorrect' })
    }
    
    // Créer les tokens
    const accessToken = createAccessToken({ id: user.id, role: user.role })
    const refreshToken = createRefreshToken({ id: user.id, role: user.role })
    
    // Cookies sécurisés
    res.cookie('access_token', accessToken, {
        httpOnly: true, 
        secure: true, 
        sameSite: 'strict', 
        maxAge: 15 * 60 * 1000
    })
    
    res.cookie('refresh_token', refreshToken, {
        httpOnly: true, 
        secure: true, 
        sameSite: 'strict', 
        maxAge: 7 * 24 * 60 * 60 * 1000
    })
    
    res.json({ 
        message: 'Authentification réussie', 
        user: { 
            email: user.email, 
            role: user.role,
            statut_utilisateur: user.statut
        } 
    })
})

// POST /api/auth/logout
router.post('/logout', (_req, res) => {
    res.clearCookie('access_token')
    res.clearCookie('refresh_token')
    res.json({ message: 'Déconnexion réussie' })
})

// POST /api/auth/register
router.post('/register', async (req, res) => {
    const { email, password } = req.body
    
    if (!email || !password) {
        return res.status(400).json({ error: 'Email et mot de passe requis' })
    }
    
    // Validation basique de l'email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email)) {
        return res.status(400).json({ error: 'Adresse e-mail invalide : merci d’utiliser le format nom@domaine.tld' })
    }
    
    // Hasher le mot de passe
    const hashed = await bcrypt.hash(password, 10)
    
    try {
        const { rows } = await pool.query(
            `INSERT INTO Utilisateur (email, password_hash, role, statut)
            VALUES ($1, $2, 'benevole', 'en_attente')
            RETURNING id, email, role, statut, date_demande`,
            [email, hashed]
        )
        
        res.status(201).json({ 
            message: 'Demande de compte créée avec succès. En attente de validation par un administrateur.',
            user: rows[0] 
        })
    } catch (err: any) {
        if (err.code === '23505') {
            return res.status(409).json({ error: 'Un compte avec cet email existe déjà' })
        }
        console.error(err)
        res.status(500).json({ error: 'Erreur serveur' })
    }
})

// POST /api/auth/refresh
router.post('/refresh', (req, res) => {
    const refresh = req.cookies?.refresh_token
    
    if (!refresh) {
        return res.status(401).json({ error: 'Refresh token manquant' })
    }
    
    try {
        const decoded = jwt.verify(refresh, process.env.JWT_SECRET || '') as TokenPayload
        const newAccessToken = createAccessToken({ id: decoded.id, role: decoded.role })
        
        res.cookie('access_token', newAccessToken, {
            httpOnly: true, 
            secure: true, 
            sameSite: 'strict', 
            maxAge: 15 * 60 * 1000
        })
        
        res.json({ message: 'Token rafraîchi' })
    } catch {
        res.status(403).json({ error: 'Refresh token invalide ou expiré' })
    }
})

// GET /api/auth/whoami
router.get('/whoami', verifyToken, async (req, res) => {
    if (!req.user) {
        return res.json({ user: null });
    }
    try {
        const { rows } = await pool.query(
            `SELECT id, email, role, statut 
             FROM Utilisateur 
             WHERE id = $1`,
            [req.user.id]
        );

        res.json({ user: rows[0] ?? null });
    } catch (err) {
        console.error('whoami error:', err);
        res.json({ user: null });
    }
});

export default router