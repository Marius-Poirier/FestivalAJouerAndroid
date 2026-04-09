package com.example.frontend.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "editeurs")
data class EditeurEntity(
    @PrimaryKey
    val id: Int,

    val nom: String,

    @ColumnInfo(name = "logo_url") val logoUrl: String?
)
