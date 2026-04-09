package com.example.frontend.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "reservation_tables", primaryKeys = ["reservation_id", "table_id"])
data class ReservationTableEntity(
    @ColumnInfo(name = "reservation_id") 
    val reservationId: Int,

    @ColumnInfo(name = "table_id")       
    val tableId: Int
)
