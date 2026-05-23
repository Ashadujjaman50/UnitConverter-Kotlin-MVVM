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
        val existingIds = existingCategories.map { it.id }.toSet()

        val defaultCategories = listOf(
            CategoryEntity("LENGTH", "Length", iconName = "straighten"),
            CategoryEntity("WEIGHT", "Weight", iconName = "fitness_center"),
            CategoryEntity("TEMPERATURE", "Temperature", iconName = "thermostat"),
            CategoryEntity("VOLUME", "Volume", iconName = "local_drink"),
            CategoryEntity("AREA", "Area", iconName = "grid_view"),
            CategoryEntity("TIME", "Time", iconName = "schedule"),
            CategoryEntity("SPEED", "Speed", iconName = "speed"),
            CategoryEntity("PRESSURE", "Pressure", iconName = "air"),
            CategoryEntity("FORCE", "Force", iconName = "fitness_center"),
            CategoryEntity("WORK", "Work", iconName = "flash"),
            CategoryEntity("POWER", "Power", iconName = "bolt"),
            CategoryEntity("DATA", "Data", iconName = "storage"),
            CategoryEntity("TORQUE", "Torque", iconName = "build"),
            CategoryEntity("AGE_CALC", "Age Calculation", iconName = "cake")
        )

        val categoriesToInsert = defaultCategories.filter { it.id !in existingIds }
        if (categoriesToInsert.isNotEmpty()) {
            converterDao.insertCategories(categoriesToInsert)
        }

        val existingUnits = converterDao.getAllUnits()
        val existingUnitIds = existingUnits.map { it.id }.toSet()

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
            UnitEntity("AREA_ha", "AREA", "Hectare", "ha", 10000.0),

            // ---- Time ----
            UnitEntity("TIME_ms", "TIME", "Millisecond", "ms", 0.001),
            UnitEntity("TIME_s", "TIME", "Second", "s", 1.0),
            UnitEntity("TIME_min", "TIME", "Minute", "min", 60.0),
            UnitEntity("TIME_hr", "TIME", "Hour", "h", 3600.0),
            UnitEntity("TIME_day", "TIME", "Day", "d", 86400.0),
            UnitEntity("TIME_week", "TIME", "Week", "wk", 604800.0),
            UnitEntity("TIME_month", "TIME", "Month", "mo", 2629746.0),
            UnitEntity("TIME_year", "TIME", "Year", "yr", 31556952.0),

            // ---- Speed ----
            UnitEntity("SPEED_mps", "SPEED", "Meter per second", "m/s", 1.0),
            UnitEntity("SPEED_kmh", "SPEED", "Kilometer per hour", "km/h", 0.2777777778),
            UnitEntity("SPEED_mph", "SPEED", "Mile per hour", "mph", 0.44704),
            UnitEntity("SPEED_fps", "SPEED", "Foot per second", "fps", 0.3048),
            UnitEntity("SPEED_knot", "SPEED", "Knot", "kt", 0.514444),

            // ---- Pressure ----
            UnitEntity("PRES_pa", "PRESSURE", "Pascal", "Pa", 1.0),
            UnitEntity("PRES_kpa", "PRESSURE", "Kilopascal", "kPa", 1000.0),
            UnitEntity("PRES_bar", "PRESSURE", "Bar", "bar", 100000.0),
            UnitEntity("PRES_psi", "PRESSURE", "Pound per sq inch", "psi", 6894.75729),
            UnitEntity("PRES_atm", "PRESSURE", "Atmosphere", "atm", 101325.0),
            UnitEntity("PRES_torr", "PRESSURE", "Torr / mmHg", "Torr", 133.322),

            // ---- Force ----
            UnitEntity("FORCE_n", "FORCE", "Newton", "N", 1.0),
            UnitEntity("FORCE_kn", "FORCE", "Kilonewton", "kN", 1000.0),
            UnitEntity("FORCE_dyn", "FORCE", "Dyne", "dyn", 0.00001),
            UnitEntity("FORCE_lbf", "FORCE", "Pound-force", "lbf", 4.4482216),
            UnitEntity("FORCE_kgf", "FORCE", "Kilogram-force", "kgf", 9.80665),

            // ---- Work ----
            UnitEntity("WORK_j", "WORK", "Joule", "J", 1.0),
            UnitEntity("WORK_kj", "WORK", "Kilojoule", "kJ", 1000.0),
            UnitEntity("WORK_mj", "WORK", "Megajoule", "MJ", 1000000.0),
            UnitEntity("WORK_cal", "WORK", "Calorie", "cal", 4.184),
            UnitEntity("WORK_kcal", "WORK", "Kilocalorie", "kcal", 4184.0),
            UnitEntity("WORK_wh", "WORK", "Watt-hour", "Wh", 3600.0),
            UnitEntity("WORK_kwh", "WORK", "Kilowatt-hour", "kWh", 3600000.0),
            UnitEntity("WORK_btu", "WORK", "British Thermal Unit", "BTU", 1055.056),

            // ---- Power ----
            UnitEntity("POWER_w", "POWER", "Watt", "W", 1.0),
            UnitEntity("POWER_kw", "POWER", "Kilowatt", "kW", 1000.0),
            UnitEntity("POWER_mw", "POWER", "Megawatt", "MW", 1000000.0),
            UnitEntity("POWER_hp_m", "POWER", "Horsepower (Metric)", "hp(M)", 735.49875),
            UnitEntity("POWER_hp_u", "POWER", "Horsepower (Imperial)", "hp(I)", 745.69987),

            // ---- Data ----
            UnitEntity("DATA_b", "DATA", "Byte", "B", 1.0),
            UnitEntity("DATA_kbit", "DATA", "Kilobit", "Kb", 128.0),
            UnitEntity("DATA_kb", "DATA", "Kilobyte", "KB", 1024.0),
            UnitEntity("DATA_mbit", "DATA", "Megabit", "Mb", 131072.0),
            UnitEntity("DATA_mb", "DATA", "Megabyte", "MB", 1048576.0),
            UnitEntity("DATA_gb", "DATA", "Gigabyte", "GB", 1073741824.0),
            UnitEntity("DATA_tb", "DATA", "Terabyte", "TB", 1099511627776.0),

            // ---- Torque ----
            UnitEntity("TORQUE_nm", "TORQUE", "Newton-meter", "N·m", 1.0),
            UnitEntity("TORQUE_knm", "TORQUE", "Kilonewton-meter", "kN·m", 1000.0),
            UnitEntity("TORQUE_lbft", "TORQUE", "Pound-force foot", "lb·ft", 1.3558179),
            UnitEntity("TORQUE_kgfm", "TORQUE", "Kilogram-force meter", "kgf·m", 9.80665),

            // ---- Age Calculation ----
            UnitEntity("AGE_CALC_years", "AGE_CALC", "Years", "yrs", 1.0)
        )

        val unitsToInsert = defaultUnits.filter { it.id !in existingUnitIds }
        if (unitsToInsert.isNotEmpty()) {
            converterDao.insertUnits(unitsToInsert)
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
