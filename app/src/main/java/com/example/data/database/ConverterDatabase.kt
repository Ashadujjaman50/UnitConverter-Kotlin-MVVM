package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ConverterDao
import com.example.data.entities.CategoryEntity
import com.example.data.entities.HistoryEntity
import com.example.data.entities.UnitEntity

@Database(
    entities = [CategoryEntity::class, UnitEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ConverterDatabase : RoomDatabase() {
    abstract fun converterDao(): ConverterDao

    companion object {
        @Volatile
        private var INSTANCE: ConverterDatabase? = null

        fun getDatabase(context: Context): ConverterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConverterDatabase::class.java,
                    "converter_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
