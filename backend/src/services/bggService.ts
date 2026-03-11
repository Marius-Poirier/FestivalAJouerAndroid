import 'dotenv/config';
import axios from 'axios';
import { XMLParser } from 'fast-xml-parser';
import pool from '../db/database.js';

const HOT_LIMIT = 100;
const BGG_API_BASE = 'https://boardgamegeek.com/xmlapi2';
// We use the token from .env if available, BGG generally blocks AWS/Azure IPs without it/User-Agent
const BGG_TOKEN = process.env.BGG_TOKEN;

const parser = new XMLParser({
  ignoreAttributes: false,
  attributeNamePrefix: '@_',
});

const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

async function retryWithBackoff<T>(fn: () => Promise<T>, retries = 3, delay = 2000): Promise<T> {
  try {
    return await fn();
  } catch (error: any) {
    if (retries > 0 && error.response?.status === 429) {
      console.warn(`Rate limit hit (429). Waiting ${delay}ms...`);
      await sleep(delay);
      return retryWithBackoff(fn, retries - 1, delay * 2);
    }
    throw error;
  }
}

const toInt = (value: unknown) => {
  const n = Number.parseInt(String(value), 10);
  return Number.isFinite(n) ? n : null;
};

const pickPrimaryName = (name: any) => name || 'Unknown';

async function fetchHot() {
  try {
    const headers: Record<string, string> = {
      'User-Agent': 'Mozilla/5.0 (compatible; FestivalAJouer/1.0)',
    };
    if (BGG_TOKEN) {
      headers['Authorization'] = `Bearer ${BGG_TOKEN}`;
    }

    const { data } = await axios.get(`${BGG_API_BASE}/hot?type=boardgame`, { headers });
    const parsed = parser.parse(data);

    const items = parsed.items?.item;
    if (!items) return [];

    const itemsArray = Array.isArray(items) ? items : [items];

    // Map to expected structure (we just need gameId essentially)
    return itemsArray.slice(0, HOT_LIMIT).map((item: any) => ({
      gameId: item['@_id'],
      name: item.name?.['@_value'],
      yearPublished: item.yearpublished?.['@_value']
    }));
  } catch (error) {
    console.error('Error fetching hot list:', error);
    return [];
  }
}

export async function fetchThing(id: number) {
  try {
    const headers: Record<string, string> = {
      'User-Agent': 'Mozilla/5.0 (compatible; FestivalAJouer/1.0)',
    };
    if (BGG_TOKEN) {
      headers['Authorization'] = `Bearer ${BGG_TOKEN}`;
    }

    const data = await retryWithBackoff(async () => {
      const response = await axios.get(`${BGG_API_BASE}/thing?id=${id}&stats=1`, { headers });
      return response.data;
    }, 5, 3000); // 5 retries, start with 3s delay

    const parsed = parser.parse(data);

    const item = parsed.items?.item;
    if (!item) return null;

    const game = Array.isArray(item) ? item[0] : item;

    let primaryName = 'Unknown';
    if (Array.isArray(game.name)) {
      const found = game.name.find((n: any) => n['@_type'] === 'primary');
      primaryName = found ? found['@_value'] : game.name[0]['@_value'];
    } else {
      primaryName = game.name['@_value'];
    }

    return {
      gameId: game['@_id'],
      name: primaryName,
      minPlayers: game.minplayers?.['@_value'],
      maxPlayers: game.maxplayers?.['@_value'],
      playingTime: game.playingtime?.['@_value'],
      minAge: game.minage?.['@_value'],
      description: game.description,
      thumbnail: game.thumbnail,
      image: game.image
    };
  } catch (error) {
    console.error(`Error fetching thing ${id}:`, error);
    return null;
  }
}

async function fetchThingsBatch(ids: number[]) {
  if (ids.length === 0) return [];
  try {
    const headers: Record<string, string> = {
      'User-Agent': 'Mozilla/5.0 (compatible; FestivalAJouer/1.0)',
    };
    if (BGG_TOKEN) {
      headers['Authorization'] = `Bearer ${BGG_TOKEN}`;
    }

    const idString = ids.join(',');

    const data = await retryWithBackoff(async () => {
      const response = await axios.get(`${BGG_API_BASE}/thing?id=${idString}&stats=1`, { headers });
      return response.data;
    }, 5, 3000);

    const parsed = parser.parse(data);

    const items = parsed.items?.item;
    if (!items) return [];

    const itemsArray = Array.isArray(items) ? items : [items];

    return itemsArray.map((game: any) => {
      let primaryName = 'Unknown';
      if (Array.isArray(game.name)) {
        const found = game.name.find((n: any) => n['@_type'] === 'primary');
        primaryName = found ? found['@_value'] : game.name[0]['@_value'];
      } else {
        primaryName = game.name?.['@_value'] ?? 'Unknown';
      }

      return {
        gameId: game['@_id'],
        name: primaryName,
        minPlayers: game.minplayers?.['@_value'],
        maxPlayers: game.maxplayers?.['@_value'],
        playingTime: game.playingtime?.['@_value'],
        minAge: game.minage?.['@_value'],
        description: game.description,
        thumbnail: game.thumbnail,
        image: game.image
      };
    });
  } catch (error) {
    console.error(`Error fetching things batch:`, error);
    return [];
  }
}

export async function fetchSearch(query: string) {
  try {
    const headers: Record<string, string> = {
      'User-Agent': 'Mozilla/5.0 (compatible; FestivalAJouer/1.0)',
    };
    if (BGG_TOKEN) {
      headers['Authorization'] = `Bearer ${BGG_TOKEN}`;
    }

    // Search specifically for boardgames
    const url = `${BGG_API_BASE}/search?query=${encodeURIComponent(query)}&type=boardgame`;

    const data = await retryWithBackoff(async () => {
      const response = await axios.get(url, { headers });
      return response.data;
    }, 5, 3000);

    const parsed = parser.parse(data);

    const items = parsed.items?.item;
    if (!items) return null;

    const itemsArray = Array.isArray(items) ? items : [items];
    return itemsArray[0]?.['@_id'];
  } catch (error) {
    console.error(`Error searching for "${query}":`, error);
    return null;
  }
}

export async function backfillGameImages() {
  console.log('Starting BGG image backfill...');
  try {
    // 1. Get games with missing images
    const { rows: games } = await pool.query(
      `SELECT id, nom, lien_regles FROM Jeu WHERE url_image IS NULL OR url_image = ''`
    );

    console.log(`Found ${games.length} games to process.`);
    if (games.length === 0) return { updated: 0, total: 0 };

    let updated = 0;

    for (const game of games) {
      let bggId: number | null = null;
      let usedSearch = false;

      // Try to extract ID from lien_regles
      if (game.lien_regles && game.lien_regles.includes('boardgamegeek.com/boardgame/')) {
        const match = game.lien_regles.match(/boardgamegeek\.com\/boardgame\/(\d+)/);
        if (match) {
          bggId = parseInt(match[1], 10);
        }
      }

      // If no ID found, search by name
      if (!bggId) {
        const searchId = await fetchSearch(game.nom);
        if (searchId) {
          bggId = parseInt(searchId, 10);
          usedSearch = true;
        }
        // Polite delay for search - enforce 3s
        await sleep(3000);
      }

      if (bggId) {
        const details = await fetchThing(bggId);
        if (details && (details.image || details.thumbnail)) {
          const imageUrl = details.image || details.thumbnail;
          await pool.query('UPDATE Jeu SET url_image = $1 WHERE id = $2', [imageUrl, game.id]);
          console.log(`> Updated "${game.nom}" with image: ${imageUrl}`);
          updated++;
        } else {
          console.log(`> No image found for "${game.nom}" (BGG ID: ${bggId})`);
        }

        // Delay after fetchThing - enforce 3s
        await sleep(3000);
      } else {
        console.log(`> Could not find BGG ID for "${game.nom}"`);
        // even if we didn't call fetchThing, we might have called fetchSearch
        if (usedSearch) await sleep(3000);
      }
    }

    return { updated, total: games.length };

  } catch (error) {
    console.error('Error during backfill:', error);
    throw error;
  }
}

export async function populateDatabase() {
  try {
    // Sync sequence to avoid "duplicate key value" errors
    try {
      await pool.query("SELECT setval('jeu_id_seq', COALESCE((SELECT MAX(id) FROM Jeu), 1))");
      console.log('Database sequence synchronized.');
    } catch (seqError) {
      console.warn('Could not sync sequence (might be first run or different schema):', seqError);
    }

    const hotItems = await fetchHot();
    if (!hotItems.length) {
      console.warn('No items found in BGG hot list.');
      return { inserted: 0, skipped: 0, total: 0 };
    }

    const allGameIds = hotItems.map((item: any) => Number(item.gameId)).filter((id: number) => !isNaN(id));

    // Process in chunks of 20 to be safe with URL length and server load
    const CHUNK_SIZE = 20;
    const games = [] as any[];

    for (let i = 0; i < allGameIds.length; i += CHUNK_SIZE) {
      const chunk = allGameIds.slice(i, i + CHUNK_SIZE);
      const batchDetails = await fetchThingsBatch(chunk);
      games.push(...batchDetails);

      // Delay between batches
      if (i + CHUNK_SIZE < allGameIds.length) {
        await sleep(4000);
      }
    }

    let inserted = 0;
    let skipped = 0;

    for (const game of games) {
      const nom = pickPrimaryName(game.name);

      const existing = await pool.query('SELECT id FROM Jeu WHERE nom = $1 LIMIT 1', [nom]);
      if (existing.rows.length > 0) {
        console.log(`> Skipped (exists): "${nom}"`);
        skipped += 1;
        continue;
      }

      const sql = `
        INSERT INTO Jeu (
          nom,
          nb_joueurs_min,
          nb_joueurs_max,
          duree_minutes,
          age_min,
          age_max,
          description,
          lien_regles,
          url_image,
          url_video,
          prototype
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
      `;

      const values = [
        nom,
        toInt(game.minPlayers),
        toInt(game.maxPlayers),
        toInt(game.playingTime),
        toInt(game.minAge),
        null,
        game.description || null,
        `https://boardgamegeek.com/boardgame/${game.gameId}`,
        game.thumbnail || game.image || null,
        null,
        false,
      ];

      await pool.query(sql, values);
      console.log(`> Inserted: "${nom}"`);
      inserted += 1;
    }

    return { inserted, skipped, total: games.length };
  } catch (err: any) {
    console.error('BGG import failed (network or DNS). Set BGG_JSON_BASE if using a proxy or mirror.', {
      message: err?.message,
      code: err?.code,
      hostname: err?.hostname,
    });
    return { inserted: 0, skipped: 0, total: 0 };
  }
}

if (import.meta.url === `file://${process.argv[1]}`) {
  populateDatabase()
    .then((result) => {
      const summary = result
        ? `BGG import finished. Inserted: ${result.inserted}, skipped: ${result.skipped}, total processed: ${result.total}`
        : 'BGG import finished.';
      console.log(summary);
      process.exit(0);
    })
    .catch((err) => {
      console.error('BGG import failed:', err?.message || err);
      process.exit(1);
    });
}