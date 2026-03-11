import { backfillGameImages } from '../services/bggService.js';

console.log('Running BGG Image Backfill...');

backfillGameImages()
    .then((result) => {
        console.log(`Backfill complete. Updated: ${result.updated}/${result.total}`);
        process.exit(0);
    })
    .catch((err) => {
        console.error('Backfill failed:', err);
        process.exit(1);
    });
