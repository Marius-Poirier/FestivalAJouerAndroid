package com.example.frontend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.database.entity.EditeurEntity

@Dao
interface EditeurDao {

    // Insère tous les éditeurs dans Room
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(editeurs: List<EditeurEntity>)

    //filtre pour recupere que les editeur qui on fait une reservation dans ce festival
    @Query("""
        SELECT DISTINCT e.* FROM editeurs e
        INNER JOIN reservations r ON r.editeur_id = e.id
        WHERE r.festival_id = :festivalId
          AND r.statut_workflow NOT IN ('pas_contacte', 'contact_pris')
        ORDER BY e.nom ASC
    """)
    suspend fun getByFestivalFiltered(festivalId: Int): List<EditeurEntity>

    // Vide la table
    @Query("DELETE FROM editeurs")
    suspend fun deleteAll()
}
