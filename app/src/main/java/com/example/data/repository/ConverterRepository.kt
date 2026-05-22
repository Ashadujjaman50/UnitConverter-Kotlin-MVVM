package com.example.data.repository

import android.content.Context
import com.example.data.dao.ConverterDao
import com.example.data.entities.CategoryEntity
import com.example.data.entities.HistoryEntity
import com.example.data.entities.UnitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ConverterRepository(private val converterDao: ConverterDao, private val context: Context) {

    private val prefs = context.getSharedPreferences("uniconvert_prefs", Context.MODE_PRIVATE)

    fun getThemeMode(): String {
        return prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun getAppLanguage(): String {
        return prefs.getString("app_language", "ENGLISH") ?: "ENGLISH"
    }

    fun setAppLanguage(language: String) {
        prefs.edit().putString("app_language", language).apply()
    }

    val allCategoriesFlow: Flow<List<CategoryEntity>> = converterDao.getAllCategoriesFlow()
    val enabledCategoriesFlow: Flow<List<CategoryEntity>> = converterDao.getEnabledCategoriesFlow()
    val allUnitsFlow: Flow<List<UnitEntity>> = converterDao.getAllUnitsFlow()
    val historyFlow: Flow<List<HistoryEntity>> = converterDao.getHistoryFlow()

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val existingCategories = converterDao.getAllCategories()
        if (existingCategories.isEmpty()) {
            val defaultCategories = listOf(
                CategoryEntity("LENGTH", "Length", iconName = "straighten"),
                CategoryEntity("WEIGHT", "Weight", iconName = "fitness_center"),
                CategoryEntity("TEMPERATURE", "Temperature", iconName = "thermostat"),
                CategoryEntity("VOLUME", "Volume", iconName = "local_drink"),
                CategoryEntity("AREA", "Area", iconName = "grid_view")
            )
            converterDao.insertCategories(defaultCategories)

            val defaultUnits = listOf(
                // ---- Length ----
                UnitEntity("LENGTH_m", "LENGTH", "Meter", "m", 1.0),
                UnitEntity("LENGTH_km", "LENGTH", "Kilometer", "km", 1000.0),
                UnitEntity("LENGTH_cm", "LENGTH", "Centimeter", "cm", 0.01),
                UnitEntity("LENGTH_mm", "LENGTH", "Millimeter", "mm", 0.001),
                UnitEntity("LENGTH_mi", "LENGTH", "Mile", "mi", 1609.344),
                UnitEntity("LENGTH_yd", "LENGTH", "Yard", "yd", 0.9144),
                UnitEntity("LENGTH_ft", "LENGTH", "Foot", "ft", 0.3048),
                UnitEntity("LENGTH_in", "LENGTH", "Inch", "in", 0.0254),

                // ---- Weight ----
                UnitEntity("WEIGHT_kg", "WEIGHT", "Kilogram", "kg", 1.0),
                UnitEntity("WEIGHT_g", "WEIGHT", "Gram", "g", 0.001),
                UnitEntity("WEIGHT_mg", "WEIGHT", "Milligram", "mg", 0.000001),
                UnitEntity("WEIGHT_lb", "WEIGHT", "Pound", "lb", 0.45359237),
                UnitEntity("WEIGHT_oz", "WEIGHT", "Ounce", "oz", 0.0283495231),
                UnitEntity("WEIGHT_ton", "WEIGHT", "Metric Ton", "t", 1000.0),

                // ---- Temperature ----
                UnitEntity("TEMP_c", "TEMPERATURE", "Celsius", "°C", 1.0, 0.0),
                UnitEntity("TEMP_f", "TEMPERATURE", "Fahrenheit", "°F", 5.0 / 9.0, 32.0),
                UnitEntity("TEMP_k", "TEMPERATURE", "Kelvin", "K", 1.0, 273.15),

                // ---- Volume ----
                UnitEntity("VOL_l", "VOLUME", "Liter", "L", 1.0),
                UnitEntity("VOL_ml", "VOLUME", "Milliliter", "mL", 0.001),
                UnitEntity("VOL_m3", "VOLUME", "Cubic Meter", "m³", 1000.0),
                UnitEntity("VOL_gal", "VOLUME", "Gallon (US)", "gal", 3.78541),
                UnitEntity("VOL_qt", "VOLUME", "Quart (US)", "qt", 0.94635),
                UnitEntity("VOL_pt", "VOLUME", "Pint (US)", "pt", 0.47318),
                UnitEntity("VOL_cup", "VOLUME", "Cup (US)", "cup", 0.23659),
                UnitEntity("VOL_floz", "VOLUME", "Fluid Ounce (US)", "fl oz", 0.02957),

                // ---- Area ----
                UnitEntity("AREA_m2", "AREA", "Square Meter", "m²", 1.0),
                UnitEntity("AREA_km2", "AREA", "Square Kilometer", "km²", 1000000.0),
                UnitEntity("AREA_mi2", "AREA", "Square Mile", "mi²", 2589988.11),
                UnitEntity("AREA_ft2", "AREA", "Square Foot", "ft²", 0.092903),
                UnitEntity("AREA_ac", "AREA", "Acre", "ac", 4046.856),
                UnitEntity("AREA_ha", "AREA", "Hectare", "ha", 10000.0)
            )
            converterDao.insertUnits(defaultUnits)
        }
    }

    suspend fun getUnitsByCategory(categoryId: String): List<UnitEntity> = withContext(Dispatchers.IO) {
        converterDao.getUnitsByCategory(categoryId)
    }

    fun getUnitsByCategoryFlow(categoryId: String): Flow<List<UnitEntity>> {
        return converterDao.getUnitsByCategoryFlow(categoryId)
    }

    suspend fun insertCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        converterDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        converterDao.updateCategory(category)
    }

    suspend fun deleteCategory(categoryId: String) = withContext(Dispatchers.IO) {
        converterDao.deleteUnitsByCategory(categoryId)
        converterDao.deleteCategory(categoryId)
    }

    suspend fun deleteUnit(unitId: String) = withContext(Dispatchers.IO) {
        converterDao.deleteUnit(unitId)
    }

    suspend fun insertUnit(unit: UnitEntity) = withContext(Dispatchers.IO) {
        converterDao.insertUnit(unit)
    }

    suspend fun insertHistory(history: HistoryEntity) = withContext(Dispatchers.IO) {
        converterDao.insertHistory(history)
    }

    suspend fun deleteHistoryItem(id: Int) = withContext(Dispatchers.IO) {
        converterDao.deleteHistoryItem(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        converterDao.clearHistory()
    }
}
