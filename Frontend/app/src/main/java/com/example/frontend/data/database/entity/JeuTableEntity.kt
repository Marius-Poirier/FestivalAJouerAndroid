package com.example.frontend.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "jeux_table")
data class JeuTableEntity(
    @PrimaryKey
    val id: Int,

    // On stocke aussi l'ID de la table à laquelle ce jeu est rattaché,
    // pour pouvoir faire la requête "quels jeux sont sur la table X ?"
    @ColumnInfo(name = "table_id") val tableId: Int,

    val nom: String?,
    @ColumnInfo(name = "url_image")    val urlImage: String?,
    @ColumnInfo(name = "type_jeu_nom") val typeJeuNom: String?,
    val editeurs: String?,
    @ColumnInfo(name = "age_min")      val ageMin: Int?,
    @ColumnInfo(name = "age_max")      val ageMax: Int?,
    val theme: String?
)
