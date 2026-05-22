package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val enabled: Boolean = true,
    val isCustom: Boolean = false,
    val iconName: String
)
