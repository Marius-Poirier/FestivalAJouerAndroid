package com.example.frontend.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tables_jeu")
data class TableJeuEntity(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "zone_du_plan_id")
    val zoneDuPlanId: Int?,
    
    @ColumnInfo(name = "zone_tarifaire_id") 
    val zoneTarifaireId: Int?,
    
    @ColumnInfo(name = "capacite_jeux")     
    val capaciteJeux: Int?,
    
    @ColumnInfo(name = "nb_jeux_actuels")   
    val nbJeuxActuels: Int?,

    val statut: String?
)
