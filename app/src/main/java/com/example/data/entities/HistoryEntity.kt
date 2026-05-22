package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: String,
    val categoryName: String,
    val fromUnitName: String,
    val toUnitName: String,
    val fromValue: Double,
    val toValue: Double,
    val timestamp: Long = System.currentTimeMillis()
)
