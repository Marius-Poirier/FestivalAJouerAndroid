package com.example.frontend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.database.entity.ZoneTarifaireEntity

@Dao
interface ZoneTarifaireDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(zones: List<ZoneTarifaireEntity>)

    @Query("SELECT * FROM zones_tarifaires WHERE festival_id = :festivalId")
    suspend fun getByFestival(festivalId: Int): List<ZoneTarifaireEntity>

    @Query("DELETE FROM zones_tarifaires")
    suspend fun deleteAll()
}
