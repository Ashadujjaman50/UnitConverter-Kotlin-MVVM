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
