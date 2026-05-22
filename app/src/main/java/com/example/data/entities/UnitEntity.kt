package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val symbol: String,
    val factorToBase: Double,
    val offset: Double = 0.0,
    val isCustom: Boolean = false
)
