package com.example.frontend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.database.entity.ZoneDuPlanEntity

@Dao
interface ZoneDuPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(zones: List<ZoneDuPlanEntity>)

    @Query("SELECT * FROM zones_du_plan WHERE festival_id = :festivalId")
    suspend fun getByFestival(festivalId: Int): List<ZoneDuPlanEntity>

    @Query("DELETE FROM zones_du_plan")
    suspend fun deleteAll()
}
