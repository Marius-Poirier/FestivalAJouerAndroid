package com.example.frontend.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_festival")
data class FestivalEntity(
    @PrimaryKey
    val id: Int,

    val nom: String,
    val lieu: String,

    @ColumnInfo(name = "date_debut") val dateDebut: String,
    @ColumnInfo(name = "date_fin")   val dateFin: String
)
