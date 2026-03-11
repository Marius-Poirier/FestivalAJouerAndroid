import type { Response, NextFunction } from 'express'

const ORGANISATEUR_ROLES = new Set(['organisateur', 'super_organisateur', 'admin'])
const FESTIVAL_VIEWER_ROLES = new Set(['benevole', 'organisateur', 'super_organisateur', 'admin'])

export function requireOrganisateur(req: Express.Request, res: Response, next: NextFunction) {
	if (!req.user || !ORGANISATEUR_ROLES.has(req.user.role)) {
		return res.status(403).json({ error: 'Accès réservé aux organisateurs' })
	}
	return next()
}

export function requireFestivalViewer(req: Express.Request, res: Response, next: NextFunction) {
	if (!req.user || !FESTIVAL_VIEWER_ROLES.has(req.user.role)) {
		return res.status(403).json({ error: 'Accès réservé aux utilisateurs connectés' })
	}
	return next()
}