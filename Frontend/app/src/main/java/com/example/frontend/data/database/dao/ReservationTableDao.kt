package com.example.frontend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.database.entity.ReservationTableEntity

@Dao
interface ReservationTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ReservationTableEntity>)

    // Retourne les ids des tables liées à une réservation donnée.
    // Ensuite on cherche chaque table par ID dans TableJeuDao.getById() pour completer les infos de la table
    // toujours utiliser cette methode pour tout recuperer
    @Query("SELECT table_id FROM reservation_tables WHERE reservation_id = :reservationId")
    suspend fun getTableIdsByReservation(reservationId: Int): List<Int>

    @Query("DELETE FROM reservation_tables")
    suspend fun deleteAll()
}
