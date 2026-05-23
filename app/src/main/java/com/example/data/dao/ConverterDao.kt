package com.example.data.dao

import androidx.room.*
import com.example.data.entities.CategoryEntity
import com.example.data.entities.HistoryEntity
import com.example.data.entities.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConverterDao {

    @Query("SELECT * FROM categories ORDER BY isCustom ASC, name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE enabled = 1")
    fun getEnabledCategoriesFlow(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId AND isCustom = 1")
    suspend fun deleteCategory(categoryId: String)

    @Query("SELECT * FROM units ORDER BY name ASC")
    fun getAllUnitsFlow(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units")
    suspend fun getAllUnits(): List<UnitEntity>

    @Query("SELECT * FROM units WHERE categoryId = :categoryId ORDER BY factorToBase ASC")
    suspend fun getUnitsByCategory(categoryId: String): List<UnitEntity>

    @Query("SELECT * FROM units WHERE categoryId = :categoryId ORDER BY factorToBase ASC")
    fun getUnitsByCategoryFlow(categoryId: String): Flow<List<UnitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitEntity)

    @Query("DELETE FROM units WHERE id = :unitId AND isCustom = 1")
    suspend fun deleteUnit(unitId: String)

    @Query("DELETE FROM units WHERE categoryId = :categoryId")
    suspend fun deleteUnitsByCategory(categoryId: String)

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getHistoryFlow(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryItem(id: Int)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
