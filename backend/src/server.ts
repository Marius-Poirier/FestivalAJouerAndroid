import fs from 'fs'
import https from 'https'
import express from 'express'
import cors from 'cors'
import morgan from 'morgan'
import cookieParser from 'cookie-parser'

import publicRouter from './routes/public.js'
import { ensureAdmin } from './db/initAdmin.js'
import usersRouter from './routes/users.js'
import festivalRouter from './routes/festival.js'
import zoneTarifaireRouter from './routes/zone-tarifaire.js'
import zoneDuPlanRouter from './routes/zone-du-plan.js'
import tablesRouter from './routes/tables.js'
import editeursRouter from './routes/editeurs.js'
import personnesRouter from './routes/personnes.js'
import jeuxRouter from './routes/jeux.js'
import reservationsRouter from './routes/reservations.js'
import reservationTablesRouter from './routes/reservation-tables.js'
import jeuFestivalRouter from './routes/jeu-festival.js'
import jeuFestivalTablesRouter from './routes/jeu-festival-tables.js'
import contactsRouter from './routes/contacts.js'
import auditRouter from './routes/audit.js'
import metadataRouter from './routes/metadata.js'
import 'dotenv/config'

import authRouter from './routes/auth.js'
import { verifyToken } from './middleware/token-management.js'
import { requireAdmin } from './middleware/auth-admin.js'
import { populateDatabase } from './services/bggService.js'
import { importCsvData } from './services/csvImportService.js';

// Création de l’application Express
const app = express()
// Ajout de l'utilisateur admin
await ensureAdmin()
// Ajout manuel des principaux en-têtes HTTP de sécurité
app.use((req, res, next) => {
    // Empêche le navigateur d’interpréter un fichier d’un autre type MIME -> attaque : XSS via upload malveillant
    res.setHeader('X-Content-Type-Options', 'nosniff')
    // Interdit l'intégration du site dans des iframes externes -> attaque : Clickjacking
    res.setHeader('X-Frame-Options', 'SAMEORIGIN')
    // Évite que les URL avec paramètres sensibles apparaissent dans les en-têtes "Referer" -> attaque : Token ou paramètres dans l’URL
    res.setHeader('Referrer-Policy', 'no-referrer')
    // Politique de ressources : seules les ressources du même site peuvent être chargées -> attaque : Fuite de données statiques
    res.setHeader('Cross-Origin-Resource-Policy', 'same-origin')
    // Politique d'ouverture inter-origine (Empêche le partage de contexte entre onglets) -> attaque : de type Spectre - isolation des fenêtres
    res.setHeader('Cross-Origin-Opener-Policy', 'same-origin')
    // Politique d'intégration inter-origine (empêche les inclusions non sûres : force l’isolation complète des ressources intégrées) -> Attaques par chargement de scripts
    res.setHeader('Cross-Origin-Embedder-Policy', 'require-corp')
    next();
})
// Configuration CORS : autoriser le front Angular en HTTPS local (MUST be before routes!)
const allowedOrigins = [
    'https://localhost:4200',  // Local Angular Dev
    'http://localhost:4200',   // Local Angular Dev (HTTP fallback)
    'https://162.38.111.37:8080', // VM Production URL
    'https://localhost:8080',
    process.env.FRONTEND_URL   // Environment variable fallback
].filter(Boolean); // Remove undefined values

app.use(cors({
    origin: (origin, callback) => {
        if (!origin) return callback(null, true);

        if (allowedOrigins.includes(origin)) {
            callback(null, true);
        } else {
            console.warn(`CORS blocked for origin: ${origin}`);
            callback(new Error('Not allowed by CORS'));
        }
    },
    credentials: true,
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));



app.use(morgan('dev')) // Log des requêtes : Visualiser le flux de requêtes entre Angular et Express
app.use(express.json())
app.use(cookieParser())

// Routes
app.use('/api/public', publicRouter)
app.use('/api/auth', authRouter)
app.use('/api/users', verifyToken, usersRouter) // protégé
app.use('/api/festivals', verifyToken, festivalRouter)
app.use('/api/zones-tarifaires', verifyToken, zoneTarifaireRouter)
app.use('/api/zones-du-plan', verifyToken, zoneDuPlanRouter)
app.use('/api/tables', verifyToken, tablesRouter)
app.use('/api/editeurs', verifyToken, editeursRouter)
app.use('/api/personnes', verifyToken, personnesRouter)
app.use('/api/jeux', verifyToken, jeuxRouter)
app.use('/api/reservations', verifyToken, reservationsRouter)
app.use('/api/reservation-tables', verifyToken, reservationTablesRouter)
app.use('/api/jeu-festival', verifyToken, jeuFestivalRouter)
app.use('/api/jeu-festival-tables', verifyToken, jeuFestivalTablesRouter)
app.use('/api/contacts', verifyToken, contactsRouter)
app.use('/api/audit', verifyToken, auditRouter)
app.use('/api/admin', verifyToken, requireAdmin, (req, res) => {
    res.json({ message: 'Bienvenue admin' });
})
app.use('/api/metadata', metadataRouter)

// Chargement du certificat et clé générés par mkcert (étape 0)
const key = fs.readFileSync('./certs/localhost-key.pem')
const cert = fs.readFileSync('./certs/localhost.pem')

// Lancement du serveur HTTPS
https.createServer({ key, cert }, app).listen(4000, () => {
    console.log('Serveur API démarré sur https://localhost:4000')
    importCsvData()
        .then(() => console.log('CSV Import process finished.'))
        .catch(err => console.error('CSV Import failed:', err));

    // Auto-run BGG image backfill in background
/*    import('./services/bggService.js').then(({ backfillGameImages }) => {
        backfillGameImages()
            .then(result => console.log(`Startup BGG Backfill: Updated ${result.updated} images out of ${result.total} candidates.`))
            .catch(err => console.error('Startup BGG Backfill failed:', err));
    });*/
})