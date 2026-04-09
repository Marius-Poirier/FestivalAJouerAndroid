package com.example.frontend.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "zones_tarifaires")
data class ZoneTarifaireEntity(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "festival_id")        
    val festivalId: Int,
    
    val nom: String,
    
    @ColumnInfo(name = "nombre_tables_total") 
    val nombreTablesTotal: Int,
    
    @ColumnInfo(name = "prix_table")         
    val prixTable: Double,
    
    @ColumnInfo(name = "prix_m2")            
    val prixM2: Double
)
