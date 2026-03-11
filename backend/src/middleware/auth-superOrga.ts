import type { Response, NextFunction } from 'express'

const SUPER_ORGA_ROLES = new Set(['super_organisateur', 'admin'])

// --- Middleware d'autorisation pour les super organisateurs ---
export function requireSuperOrga(req: Express.Request, res: Response, next: NextFunction) {
    if (!req.user || !SUPER_ORGA_ROLES.has(req.user.role)) {
        return res.status(403).json({ error: 'Accès réservé aux super organisateurs' })
    }
    next()
}