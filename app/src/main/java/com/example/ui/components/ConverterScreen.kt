package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entities.CategoryEntity
import com.example.data.entities.UnitEntity
import com.example.ui.viewmodel.ConverterViewModel
import com.example.ui.viewmodel.AppLanguage
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Calendar
import android.app.DatePickerDialog

@Composable
fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "straighten" -> Icons.Default.Straighten
        "fitness_center" -> Icons.Default.FitnessCenter
        "thermostat" -> Icons.Default.Thermostat
        "local_drink" -> Icons.Default.LocalDrink
        "grid_view" -> Icons.Default.GridView
        "speed" -> Icons.Default.Speed
        "schedule" -> Icons.Default.Schedule
        "bolt" -> Icons.Default.Bolt
        "star" -> Icons.Default.Star
        "air" -> Icons.Default.Air
        "flash" -> Icons.Default.FlashOn
        "storage" -> Icons.Default.Storage
        "build" -> Icons.Default.Build
        "cake" -> Icons.Default.Cake
        else -> Icons.Default.Build
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val enabledCategories by viewModel.enabledCategories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val activeUnits by viewModel.activeUnits.collectAsStateWithLifecycle()
    val selectedFromUnit by viewModel.selectedFromUnit.collectAsStateWithLifecycle()
    val selectedToUnit by viewModel.selectedToUnit.collectAsStateWithLifecycle()
    val inputValue by viewModel.inputValue.collectAsStateWithLifecycle()
    val outputValue by viewModel.outputValue.collectAsStateWithLifecycle()

    var showFromUnitSelector by remember { mutableStateOf(false) }
    var showToUnitSelector by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }

    val animateRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "rotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Space at the top
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // --- Category Selection Row ---
        item {
            Text(
                text = "Select Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            if (enabledCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active unit types. Go to Customize to enable them!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(enabledCategories) { category ->
                        val isSelected = selectedCategory?.id == category.id
                        CategoryChip(
                            category = category,
                            isSelected = isSelected,
                            onClick = { viewModel.selectCategory(category) }
                        )
                    }
                }
            }
        }

        // --- Core Converter Interface ---
        if (selectedCategory != null) {
            if (selectedCategory?.id == "AGE_CALC") {
                item {
                    AgeCalculatorLayout(viewModel = viewModel)
                }
            } else {
                item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // --- FROM CARD ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "From",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Unit Dropdown Trigger Button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                        .clickable { showFromUnitSelector = true }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("from_unit_selector"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedFromUnit?.symbol ?: "Select",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown Trigger",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Large Monospace input and details
                            OutlinedTextField(
                                value = inputValue,
                                onValueChange = { newValue ->
                                    // Accept only floating numbers
                                    if (newValue.isEmpty() || newValue.matches(Regex("""^-?\d*\.?\d*$"""))) {
                                        viewModel.setInputValue(newValue)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_value_field"),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 28.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                placeholder = {
                                    Text(
                                        "0.0",
                                        fontSize = 28.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )

                            Text(
                                text = selectedFromUnit?.name ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // --- VERTICAL OVERLAPPING ROTATING SWAP BUTTON ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )

                        IconButton(
                            onClick = {
                                rotationAngle += 180f
                                viewModel.swapUnits()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                )
                                .rotate(animateRotation)
                                .testTag("swap_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap conversion units",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // --- TO CARD ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "To",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                // Dropdown selector for To unit
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                        .clickable { showToUnitSelector = true }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("to_unit_selector"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedToUnit?.symbol ?: "Select",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown Trigger",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Large Read-only outputs
                            Text(
                                text = outputValue.ifEmpty { "0.0" },
                                fontSize = 32.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                                    .testTag("output_value_text")
                            )

                            Text(
                                text = selectedToUnit?.name ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- Quick Actions Bar ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Copy Result Button
                    Button(
                        onClick = {
                            if (outputValue.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(outputValue))
                                Toast.makeText(context, "Copied $outputValue ${selectedToUnit?.symbol} to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy", fontWeight = FontWeight.Bold)
                    }

                    // Log conversion to History
                    Button(
                        onClick = {
                            if (inputValue.isNotEmpty() && outputValue.isNotEmpty()) {
                                viewModel.saveConversionToHistory()
                                Toast.makeText(context, "Conversion logged offline", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                            .testTag("save_history_button")
                    ) {
                        Icon(imageVector = Icons.Default.HistoryToggleOff, contentDescription = "Log History")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Log", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- Visual Formula Context Pane ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculation info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Conversion Rule",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val fromName = selectedFromUnit?.name ?: ""
                        val fromSym = selectedFromUnit?.symbol ?: ""
                        val fromFactor = selectedFromUnit?.factorToBase ?: 1.0
                        val toName = selectedToUnit?.name ?: ""
                        val toSym = selectedToUnit?.symbol ?: ""
                        val toFactor = selectedToUnit?.factorToBase ?: 1.0

                        Text(
                            text = "Conversion uses the offline base standard. " +
                                    "1 $fromSym is ${fromFactor / toFactor} $toSym.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
          }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // --- FROM UNIT SELECTOR DIALOG ---
    if (showFromUnitSelector && selectedCategory != null) {
        UnitSelectorDialog(
            title = "Convert From",
            units = activeUnits,
            selectedUnit = selectedFromUnit,
            onUnitSelected = {
                viewModel.selectFromUnit(it)
                showFromUnitSelector = false
            },
            onDismiss = { showFromUnitSelector = false }
        )
    }

    // --- TO UNIT SELECTOR DIALOG ---
    if (showToUnitSelector && selectedCategory != null) {
        UnitSelectorDialog(
            title = "Convert To",
            units = activeUnits,
            selectedUnit = selectedToUnit,
            onUnitSelected = {
                viewModel.selectToUnit(it)
                showToUnitSelector = false
            },
            onDismiss = { showToUnitSelector = false }
        )
    }
}

@Composable
fun CategoryChip(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderStroke = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("category_chip_${category.id}"),
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getCategoryIcon(category.iconName),
                contentDescription = category.name,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelectorDialog(
    title: String,
    units: List<UnitEntity>,
    selectedUnit: UnitEntity?,
    onUnitSelected: (UnitEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUnits = remember(searchQuery, units) {
        units.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.symbol.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .testTag("unit_selector_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search unit...") },
                    modifier = Modifier.fillMaxWidth().testTag("unit_search_field"),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (filteredUnits.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No units found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredUnits) { unit ->
                                val isSelected = unit.id == selectedUnit?.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else Color.Transparent
                                        )
                                        .clickable { onUnitSelected(unit) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .testTag("unit_option_${unit.id}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = unit.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (unit.isCustom) {
                                            Text(
                                                text = "Custom",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = unit.symbol,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_dialog")) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AgeCalculatorLayout(
    viewModel: ConverterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    
    // Default DOB: 2000-01-01
    var birthDate by remember { mutableStateOf(LocalDate.of(2000, 1, 1)) }
    // Default Target Date: Today
    var targetDate by remember { mutableStateOf(LocalDate.now()) }

    // Reactive calculations
    val hasError = birthDate.isAfter(targetDate)
    
    val period = remember(birthDate, targetDate) {
        Period.between(birthDate, targetDate)
    }
    
    val ageYears = if (hasError) 0 else period.years
    val ageMonths = if (hasError) 0 else period.months
    val ageDays = if (hasError) 0 else period.days

    // Totals
    val totalYears = if (hasError) 0L else ChronoUnit.YEARS.between(birthDate, targetDate)
    val totalMonths = if (hasError) 0L else ChronoUnit.MONTHS.between(birthDate, targetDate)
    val totalWeeks = if (hasError) 0L else ChronoUnit.WEEKS.between(birthDate, targetDate)
    val totalDays = if (hasError) 0L else ChronoUnit.DAYS.between(birthDate, targetDate)
    val totalHours = totalDays * 24L
    val totalMinutes = totalHours * 60L
    val totalSeconds = totalMinutes * 60L

    // Countdown / Next birthday calculation
    val birthDateInTargetYear = remember(birthDate, targetDate) {
        try {
            birthDate.withYear(targetDate.year)
        } catch (e: Exception) {
            // Leap year handle fallback (e.g. Feb 29)
            birthDate.plusDays(1).withYear(targetDate.year)
        }
    }
    
    val nextBirthdayDate = remember(birthDateInTargetYear, targetDate) {
        if (birthDateInTargetYear.isBefore(targetDate) || birthDateInTargetYear.isEqual(targetDate)) {
            try {
                birthDate.withYear(targetDate.year + 1)
            } catch (e: Exception) {
                birthDate.plusDays(1).withYear(targetDate.year + 1)
            }
        } else {
            birthDateInTargetYear
        }
    }

    val daysToNextBirthday = remember(targetDate, nextBirthdayDate, hasError) {
        if (hasError) 0L else ChronoUnit.DAYS.between(targetDate, nextBirthdayDate)
    }

    // Birthdays celebrated (gone)
    val birthdaysGone = ageYears

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- INPUTS ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Birth Date Picker Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val dp = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                birthDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            birthDate.year,
                            birthDate.monthValue - 1,
                            birthDate.dayOfMonth
                        )
                        dp.show()
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = LanguageResources.getString(appLanguage, "age_date_of_birth"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = birthDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pick Date of Birth",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Target Date Picker Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val dp = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                targetDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            targetDate.year,
                            targetDate.monthValue - 1,
                            targetDate.dayOfMonth
                        )
                        dp.show()
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = LanguageResources.getString(appLanguage, "age_target_date"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = targetDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Pick Target Date",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (hasError) {
            // Beautiful descriptive error card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = if (appLanguage == AppLanguage.BANGLA) "জন্মতারিখ অবশ্যই হিসাব করার তারিখের চেয়ে পূর্ববর্তী হতে হবে!"
                               else if (appLanguage == AppLanguage.SPANISH) "¡La fecha de nacimiento debe ser anterior a la de cálculo!"
                               else if (appLanguage == AppLanguage.FRENCH) "La date de naissance doit être antérieure à la date cible !"
                               else if (appLanguage == AppLanguage.GERMAN) "Das Geburtsdatum muss vor dem Berechnungsdatum liegen!"
                               else "Birth date must be earlier than target calculation date!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        } else {
            // --- OUTPUT REVOLUTION CARD: CALCULATED AGE ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = LanguageResources.getString(appLanguage, "age_result_title"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AgeUnitSegment(value = ageYears, unitLabel = LanguageResources.getString(appLanguage, "age_years"))
                        Spacer(modifier = Modifier.width(16.dp))
                        AgeUnitSegment(value = ageMonths, unitLabel = LanguageResources.getString(appLanguage, "age_months"))
                        Spacer(modifier = Modifier.width(16.dp))
                        AgeUnitSegment(value = ageDays, unitLabel = LanguageResources.getString(appLanguage, "age_days"))
                    }
                }
            }

            // --- NEXT BIRTHDAY COUNTDOWN CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = "Next Birthday",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        val isBirthdayToday = (birthDate.monthValue == targetDate.monthValue && birthDate.dayOfMonth == targetDate.dayOfMonth)
                        if (isBirthdayToday) {
                            Text(
                                text = if (appLanguage == AppLanguage.BANGLA) "শুভ জন্মদিন! 🎂🎉"
                                       else if (appLanguage == AppLanguage.SPANISH) "¡Feliz Cumpleaños! 🎂🎉"
                                       else if (appLanguage == AppLanguage.FRENCH) "Joyeux Anniversaire ! 🎂🎉"
                                       else if (appLanguage == AppLanguage.GERMAN) "Herzlichen Glückwunsch! 🎂🎉"
                                       else "Happy Birthday! 🎂🎉",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (appLanguage == AppLanguage.BANGLA) "আজকে আপনার জন্মদিন উদযাপন করুন!"
                                       else if (appLanguage == AppLanguage.SPANISH) "¡Hoy es tu día especial!"
                                       else if (appLanguage == AppLanguage.FRENCH) "C'est votre journée spéciale !"
                                       else if (appLanguage == AppLanguage.GERMAN) "Es ist dein besonderer Tag!"
                                       else "Celebrate your special day today!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = if (appLanguage == AppLanguage.BANGLA) "$daysToNextBirthday দিন বাকি"
                                       else if (appLanguage == AppLanguage.SPANISH) "Faltan $daysToNextBirthday días"
                                       else if (appLanguage == AppLanguage.FRENCH) "$daysToNextBirthday jours restants"
                                       else if (appLanguage == AppLanguage.GERMAN) "Noch $daysToNextBirthday Tage"
                                       else "$daysToNextBirthday Days Left",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = (if (appLanguage == AppLanguage.BANGLA) "পরবর্তী জন্মদিন: "
                                       else if (appLanguage == AppLanguage.SPANISH) "Próximo cumpleaños: "
                                       else if (appLanguage == AppLanguage.FRENCH) "Prochain anniversaire : "
                                       else if (appLanguage == AppLanguage.GERMAN) "Nächster Geburtstag: "
                                       else "Next Birthday: ") + nextBirthdayDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = birthdaysGone.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = if (appLanguage == AppLanguage.BANGLA) "জন্মদিন পার"
                                   else if (appLanguage == AppLanguage.SPANISH) "Cumpleaños"
                                   else if (appLanguage == AppLanguage.FRENCH) "Bougies"
                                   else if (appLanguage == AppLanguage.GERMAN) "Feste"
                                   else "Celebrated",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- CUMULATIVE STATS GRID ---
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = LanguageResources.getString(appLanguage, "age_total_summary"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Grid rows
                CumulativeStatRow(
                    label1 = LanguageResources.getString(appLanguage, "age_total_months"),
                    value1 = totalMonths.toString(),
                    label2 = LanguageResources.getString(appLanguage, "age_total_weeks"),
                    value2 = totalWeeks.toString()
                )

                CumulativeStatRow(
                    label1 = LanguageResources.getString(appLanguage, "age_total_days"),
                    value1 = totalDays.toString(),
                    label2 = LanguageResources.getString(appLanguage, "age_total_hours"),
                    value2 = java.text.NumberFormat.getIntegerInstance().format(totalHours)
                )

                CumulativeStatRow(
                    label1 = LanguageResources.getString(appLanguage, "age_total_minutes"),
                    value1 = java.text.NumberFormat.getIntegerInstance().format(totalMinutes),
                    label2 = LanguageResources.getString(appLanguage, "age_total_seconds"),
                    value2 = java.text.NumberFormat.getIntegerInstance().format(totalSeconds)
                )
            }

            // --- ACTION BUTTONS ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Save to history button
                Button(
                    onClick = {
                        val birthStr = birthDate.format(dateFormatter)
                        val targetStr = targetDate.format(dateFormatter)
                        val ageStr = "$ageYears ${LanguageResources.getString(appLanguage, "age_years")}, " +
                                     "$ageMonths ${LanguageResources.getString(appLanguage, "age_months")}, " +
                                     "$ageDays ${LanguageResources.getString(appLanguage, "age_days")}"
                        
                        viewModel.saveCustomHistoryLog(
                            categoryId = "AGE_CALC",
                            categoryName = "Age Calculation",
                            fromUnitName = "${LanguageResources.getString(appLanguage, "age_date_of_birth")}: $birthStr (At: $targetStr)",
                            toUnitName = "${LanguageResources.getString(appLanguage, "age_result_title")}: $ageStr",
                            fromValue = 0.0,
                            toValue = 0.0
                        )
                        Toast.makeText(context, LanguageResources.getString(appLanguage, "logged_toast"), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("save_history_button")
                ) {
                    Icon(imageVector = Icons.Default.HistoryToggleOff, contentDescription = "Log History")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageResources.getString(appLanguage, "save_log"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AgeUnitSegment(
    value: Int,
    unitLabel: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = unitLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CumulativeStatRow(
    label1: String,
    value1: String,
    label2: String,
    value2: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CumulativeStatField(
            modifier = Modifier.weight(1f),
            label = label1,
            value = value1
        )
        CumulativeStatField(
            modifier = Modifier.weight(1f),
            label = label2,
            value = value2
        )
    }
}

@Composable
fun CumulativeStatField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
