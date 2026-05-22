package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entities.CategoryEntity
import com.example.data.entities.HistoryEntity
import com.example.data.entities.UnitEntity
import com.example.data.repository.ConverterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AppLanguage {
    ENGLISH, SPANISH, FRENCH, GERMAN, BANGLA
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ConverterViewModel(private val repository: ConverterRepository) : ViewModel() {

    // --- Control Settings States ---
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // --- State Streams ---
    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enabledCategories: StateFlow<List<CategoryEntity>> = repository.enabledCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUnits: StateFlow<List<UnitEntity>> = repository.allUnitsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyList: StateFlow<List<HistoryEntity>> = repository.historyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<CategoryEntity?>(null)
    val selectedCategory: StateFlow<CategoryEntity?> = _selectedCategory.asStateFlow()

    // Units for the currently selected category
    val activeUnits: StateFlow<List<UnitEntity>> = _selectedCategory
        .flatMapLatest { category ->
            if (category == null) flowOf(emptyList())
            else repository.getUnitsByCategoryFlow(category.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFromUnit = MutableStateFlow<UnitEntity?>(null)
    val selectedFromUnit: StateFlow<UnitEntity?> = _selectedFromUnit.asStateFlow()

    private val _selectedToUnit = MutableStateFlow<UnitEntity?>(null)
    val selectedToUnit: StateFlow<UnitEntity?> = _selectedToUnit.asStateFlow()

    val inputValue = MutableStateFlow("1")

    // Reactive calculated output
    val outputValue: StateFlow<String> = combine(
        inputValue,
        _selectedFromUnit,
        _selectedToUnit
    ) { input, from, to ->
        performCalculation(input, from, to)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        // Load settings from persistent repository
        _themeMode.value = try {
            ThemeMode.valueOf(repository.getThemeMode())
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        _appLanguage.value = try {
            AppLanguage.valueOf(repository.getAppLanguage())
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            
            // Set first enabled category as selected by default if nothing selected yet
            repository.enabledCategoriesFlow.collectLatest { categories ->
                if (_selectedCategory.value == null && categories.isNotEmpty()) {
                    selectCategory(categories.first())
                }
            }
        }

        // Set default units when activeUnits changes
        viewModelScope.launch {
            activeUnits.collect { units ->
                if (units.isNotEmpty()) {
                    // Avoid resetting if we already have selected units that belong to the current category
                    val currentCatId = _selectedCategory.value?.id
                    val fromOk = _selectedFromUnit.value?.categoryId == currentCatId
                    val toOk = _selectedToUnit.value?.categoryId == currentCatId

                    if (!fromOk || _selectedFromUnit.value == null) {
                        _selectedFromUnit.value = units.getOrNull(0)
                    }
                    if (!toOk || _selectedToUnit.value == null) {
                        _selectedToUnit.value = units.getOrNull(1) ?: units.getOrNull(0)
                    }
                } else {
                    _selectedFromUnit.value = null
                    _selectedToUnit.value = null
                }
            }
        }
    }

    // --- Action Methods ---

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        repository.setThemeMode(mode.name)
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        repository.setAppLanguage(language.name)
    }

    fun selectCategory(category: CategoryEntity) {
        _selectedCategory.value = category
    }

    fun selectFromUnit(unit: UnitEntity) {
        _selectedFromUnit.value = unit
    }

    fun selectToUnit(unit: UnitEntity) {
        _selectedToUnit.value = unit
    }

    fun swapUnits() {
        val temp = _selectedFromUnit.value
        _selectedFromUnit.value = _selectedToUnit.value
        _selectedToUnit.value = temp
    }

    fun setInputValue(value: String) {
        inputValue.value = value
    }

    fun saveConversionToHistory() {
        val fromVal = inputValue.value.toDoubleOrNull() ?: return
        val fromUnit = _selectedFromUnit.value ?: return
        val toUnit = _selectedToUnit.value ?: return
        val outValStr = outputValue.value
        val toVal = outValStr.toDoubleOrNull() ?: return
        val category = _selectedCategory.value ?: return

        viewModelScope.launch {
            val history = HistoryEntity(
                categoryId = category.id,
                categoryName = category.name,
                fromUnitName = "${fromUnit.name} (${fromUnit.symbol})",
                toUnitName = "${toUnit.name} (${toUnit.symbol})",
                fromValue = fromVal,
                toValue = toVal
            )
            repository.insertHistory(history)
        }
    }

    fun loadHistoryItem(history: HistoryEntity) {
        viewModelScope.launch {
            val categories = allCategories.value
            val category = categories.find { it.id == history.categoryId } ?: return@launch
            
            selectCategory(category)
            
            // Wait for units to load and match history units
            val units = repository.getUnitsByCategory(history.categoryId)
            val fromUnitObj = units.find { "${it.name} (${it.symbol})" == history.fromUnitName }
            val toUnitObj = units.find { "${it.name} (${it.symbol})" == history.toUnitName }
            
            if (fromUnitObj != null) _selectedFromUnit.value = fromUnitObj
            if (toUnitObj != null) _selectedToUnit.value = toUnitObj
            
            // Format number cleanly
            val formattedFrom = if (history.fromValue % 1.0 == 0.0) {
                history.fromValue.toLong().toString()
            } else {
                history.fromValue.toString()
            }
            inputValue.value = formattedFrom
        }
    }

    fun toggleCategoryEnabled(category: CategoryEntity) {
        viewModelScope.launch {
            val updated = category.copy(enabled = !category.enabled)
            repository.updateCategory(updated)
            
            // If we disabled the currently selected category, switch to another enabled one
            if (!updated.enabled && _selectedCategory.value?.id == category.id) {
                val enabled = repository.enabledCategoriesFlow.firstOrNull() ?: emptyList()
                val nextSelected = enabled.firstOrNull { it.id != category.id }
                if (nextSelected != null) {
                    selectCategory(nextSelected)
                } else {
                    _selectedCategory.value = null
                }
            }
        }
    }

    fun addCustomCategory(name: String, iconName: String) {
        viewModelScope.launch {
            val rawId = name.uppercase().replace(" ", "_")
            val id = "CUSTOM_$rawId"
            
            val newCategory = CategoryEntity(
                id = id,
                name = name,
                isCustom = true,
                iconName = iconName
            )
            repository.insertCategory(newCategory)
            
            // Auto add a default base unit for this custom category so it functions immediately
            val defaultUnit = UnitEntity(
                id = "${id}_base",
                categoryId = id,
                name = "Base Unit",
                symbol = "unit",
                factorToBase = 1.0,
                isCustom = true
            )
            repository.insertUnit(defaultUnit)
            
            // Select the newly added category
            _selectedCategory.value = newCategory
        }
    }

    fun addCustomUnit(categoryId: String, name: String, symbol: String, factorToBase: Double, offset: Double = 0.0) {
        viewModelScope.launch {
            val rawId = name.uppercase().replace(" ", "_")
            val id = "${categoryId}_${rawId}"
            val newUnit = UnitEntity(
                id = id,
                categoryId = categoryId,
                name = name,
                symbol = symbol,
                factorToBase = factorToBase,
                offset = offset,
                isCustom = true
            )
            repository.insertUnit(newUnit)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    fun deleteUnit(unitId: String) {
        viewModelScope.launch {
            repository.deleteUnit(unitId)
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private fun performCalculation(inputStr: String, fromUnit: UnitEntity?, toUnit: UnitEntity?): String {
        if (fromUnit == null || toUnit == null) return ""
        val inputVal = inputStr.toDoubleOrNull() ?: return ""

        // Formula: BaseValue = (ValueFrom - offsetFrom) * factorToBaseFrom
        val baseValue = (inputVal - fromUnit.offset) * fromUnit.factorToBase

        // Formula: ValueTo = (BaseValue / factorToBaseTo) + offsetTo
        val converted = (baseValue / toUnit.factorToBase) + toUnit.offset

        return formatValue(converted)
    }

    private fun formatValue(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        if (value == 0.0) return "0"

        val absValue = kotlin.math.abs(value)
        return when {
            absValue >= 1e8 || absValue < 1e-4 -> {
                String.format(java.util.Locale.US, "%.4e", value)
                    .replace("e+", "e")
                    .replace("e-0", "e-")
            }
            else -> {
                val formatted = String.format(java.util.Locale.US, "%.6f", value)
                var trimmed = formatted
                if (trimmed.contains(".")) {
                    while (trimmed.endsWith("0")) {
                        trimmed = trimmed.substring(0, trimmed.length - 1)
                    }
                    if (trimmed.endsWith(".")) {
                        trimmed = trimmed.substring(0, trimmed.length - 1)
                    }
                }
                trimmed
            }
        }
    }
}

class ConverterViewModelFactory(private val repository: ConverterRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConverterViewModel::class.java)) {
            return ConverterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
