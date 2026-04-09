package com.example.frontend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.database.entity.TableJeuEntity

@Dao
interface TableJeuDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tables: List<TableJeuEntity>)

    // Toutes les tables d'une zone du plan (pour l'onglet Plan).
    @Query("SELECT * FROM tables_jeu WHERE zone_du_plan_id = :zoneDuPlanId")
    suspend fun getByZone(zoneDuPlanId: Int): List<TableJeuEntity>

    // Une table spécifique par son ID (utilisé pour les tables de réservation).
    @Query("SELECT * FROM tables_jeu WHERE id = :tableId")
    suspend fun getById(tableId: Int): TableJeuEntity?

    @Query("DELETE FROM tables_jeu")
    suspend fun deleteAll()
}
