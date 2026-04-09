package com.example.frontend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.database.entity.JeuFestivalEntity

@Dao
interface JeuFestivalDao {

    // Insère tous les jeux du festival dans Room
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jeux: List<JeuFestivalEntity>)

    // Tous les jeux d'un festival
    @Query("SELECT * FROM jeux_festival WHERE festival_id = :festivalId")
    suspend fun getByFestival(festivalId: Int): List<JeuFestivalEntity>

    // Jeux filtrés par réservation
    @Query("SELECT * FROM jeux_festival WHERE reservation_id = :reservationId")
    suspend fun getByReservation(reservationId: Int): List<JeuFestivalEntity>

    // Vide la table 
    @Query("DELETE FROM jeux_festival")
    suspend fun deleteAll()
}
