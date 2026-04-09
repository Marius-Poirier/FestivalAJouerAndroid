package com.example.frontend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.database.entity.ReservationEntity

@Dao
interface ReservationDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reservations: List<ReservationEntity>)

    // Retourne toutes les réservations d'un festival donné.
    @Query("SELECT * FROM reservations WHERE festival_id = :festivalId")
    suspend fun getByFestival(festivalId: Int): List<ReservationEntity>

    @Query("DELETE FROM reservations")
    suspend fun deleteAll()
}
