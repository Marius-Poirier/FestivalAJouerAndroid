package com.example.frontend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.frontend.data.database.entity.FestivalEntity

@Dao
interface FestivalDao {

    // ajouter et mettre a jour automatiquement le festival selectioner par l'utilisateur
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(festival: FestivalEntity)

    // Lit le seul festival stocké il y en a toujours qu'un seul
    @Query("SELECT * FROM cached_festival LIMIT 1")
    suspend fun getFestival(): FestivalEntity?

    // Vide la table avant d'en sauvegarder un nouveau
    @Query("DELETE FROM cached_festival")
    suspend fun deleteAll()
}
