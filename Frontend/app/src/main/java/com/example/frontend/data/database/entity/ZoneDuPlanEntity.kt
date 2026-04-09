package com.example.frontend.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "zones_du_plan")
data class ZoneDuPlanEntity(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "festival_id")      
    val festivalId: Int,
    
    val nom: String,
    
    @ColumnInfo(name = "nombre_tables")    
    val nombreTables: Int,
    
    @ColumnInfo(name = "zone_tarifaire_id") 
    val zoneTarifaireId: Int
)
