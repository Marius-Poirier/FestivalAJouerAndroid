package com.example.frontend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.database.entity.JeuTableEntity

@Dao
interface JeuTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jeux: List<JeuTableEntity>)

    // Recuperer les jeux sur une table donnée
    @Query("SELECT * FROM jeux_table WHERE table_id = :tableId")
    suspend fun getByTable(tableId: Int): List<JeuTableEntity>

    @Query("DELETE FROM jeux_table")
    suspend fun deleteAll()
}
