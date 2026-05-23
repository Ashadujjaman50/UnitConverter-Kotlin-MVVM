package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
fun SettingsScreen(
    viewModel: ConverterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allUnits by viewModel.allUnits.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddUnitDialog by remember { mutableStateOf(false) }
    var selectedCategoryForUnitAdd by remember { mutableStateOf<CategoryEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // --- SECTION 1: Enabled Categories Toggles ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Unit Types",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Toggle which categories show up on home.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Add Custom Category Shortcut Icon
                        IconButton(
                            onClick = { showAddCategoryDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("add_custom_category_shortcut")
                        ) {
                            Icon(imageVector = Icons.Default.AddHome, contentDescription = "Add custom category")
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    allCategories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(category.iconName),
                                        contentDescription = category.name,
                                        tint = if (category.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = category.name,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (category.isCustom) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                                contentColor = MaterialTheme.colorScheme.secondary,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Custom",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (category.isCustom) {
                                        Text(
                                            text = "Tap delete to remove permanently",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (category.isCustom) {
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteCategory(category.id)
                                            Toast.makeText(context, "Deleted category ${category.name}", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete customs")
                                    }
                                }

                                Switch(
                                    checked = category.enabled,
                                    onCheckedChange = { isChecked ->
                                         if (!isChecked) {
                                             val currentlyEnabledCount = allCategories.count { it.enabled }
                                             if (currentlyEnabledCount <= 1) {
                                                 Toast.makeText(context, LanguageResources.getString(appLanguage, "min_one_category"), Toast.LENGTH_SHORT).show()
                                             } else {
                                                 viewModel.toggleCategoryEnabled(category)
                                             }
                                         } else {
                                             viewModel.toggleCategoryEnabled(category)
                                         }
                                     },
                                    modifier = Modifier.testTag("switch_category_${category.id}"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: Custom Unit Adding ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customize Multipliers & Units",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Review conversions and add custom multiplier units.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    allCategories.forEach { category ->
                        var isExpanded by remember { mutableStateOf(false) }
                        val categoryUnits = remember(allUnits, category.id) {
                            allUnits.filter { it.categoryId == category.id }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = getCategoryIcon(category.iconName),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${category.name} (${categoryUnits.size} units)",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            selectedCategoryForUnitAdd = category
                                            showAddUnitDialog = true
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.testTag("add_unit_to_${category.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Custom Unit")
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand info"
                                    )
                                }
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(4.dp))

                                categoryUnits.forEach { unit ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = unit.name,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "[${unit.symbol}]",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            Text(
                                                text = "Factor base: ${unit.factorToBase}${if (unit.offset != 0.0) ", Offset: ${unit.offset}" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }

                                        if (unit.isCustom) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteUnit(unit.id)
                                                    Toast.makeText(context, "Deleted custom unit ${unit.name}", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                ),
                                                modifier = Modifier.testTag("delete_unit_${unit.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete customs module")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // --- DIALOG: CREATE CUSTOM CATEGORY ---
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        val iconSelection = listOf("speed", "schedule", "bolt", "star", "air", "flash", "storage", "build", "cake")
        var selectedIconIndex by remember { mutableStateOf(0) }

        Dialog(onDismissRequest = { showAddCategoryDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_category_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add Custom Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        placeholder = { Text("Category Name (e.g. Speed)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_category_name_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Text(
                        text = "Select Category Icon",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(iconSelection.size) { index ->
                            val iconName = iconSelection[index]
                            val isSelected = index == selectedIconIndex
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedIconIndex = index }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("icon_choice_$iconName"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(iconName),
                                    contentDescription = iconName,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddCategoryDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (catName.trim().isNotEmpty()) {
                                    viewModel.addCustomCategory(catName.trim(), iconSelection[selectedIconIndex])
                                    Toast.makeText(context, "Added category ${catName.trim()}", Toast.LENGTH_SHORT).show()
                                    showAddCategoryDialog = false
                                }
                            },
                            enabled = catName.trim().isNotEmpty(),
                            modifier = Modifier.testTag("confirm_add_category_button")
                        ) {
                            Text("Create", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: CREATE CUSTOM UNIT ---
    if (showAddUnitDialog && selectedCategoryForUnitAdd != null) {
        val destCategory = selectedCategoryForUnitAdd!!
        var unitName by remember { mutableStateOf("") }
        var unitSymbol by remember { mutableStateOf("") }
        var unitMultiplier by remember { mutableStateOf("1.0") }
        var unitOffset by remember { mutableStateOf("0.0") }

        Dialog(onDismissRequest = { showAddUnitDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_unit_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Add custom unit to ${destCategory.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = unitName,
                        onValueChange = { unitName = it },
                        placeholder = { Text("Unit Name (e.g. Nanometer)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_unit_name_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = unitSymbol,
                        onValueChange = { unitSymbol = it },
                        placeholder = { Text("Symbol (e.g. nm)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_unit_symbol_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = unitMultiplier,
                        onValueChange = { unitMultiplier = it },
                        placeholder = { Text("Multiplier value to Base") },
                        label = { Text("Multiplier to base (decimal factor)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_unit_multiplier_field"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Offset (mainly for Temperature modifications)
                    OutlinedTextField(
                        value = unitOffset,
                        onValueChange = { unitOffset = it },
                        placeholder = { Text("Additive offset (usually 0.0)") },
                        label = { Text("Additive Offset (if any)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_unit_offset_field"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Text(
                        text = "Calculation rule: Offset is subtracted from raw custom value and then multiplied by Factor to convert to base metric standard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Start
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddUnitDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val parsedFactor = unitMultiplier.toDoubleOrNull() ?: 1.0
                                val parsedOffset = unitOffset.toDoubleOrNull() ?: 0.0
                                if (unitName.trim().isNotEmpty() && unitSymbol.trim().isNotEmpty()) {
                                    viewModel.addCustomUnit(
                                        categoryId = destCategory.id,
                                        name = unitName.trim(),
                                        symbol = unitSymbol.trim(),
                                        factorToBase = parsedFactor,
                                        offset = parsedOffset
                                    )
                                    Toast.makeText(context, "Added custom unit ${unitName.trim()}", Toast.LENGTH_SHORT).show()
                                    showAddUnitDialog = false
                                }
                            },
                            enabled = unitName.trim().isNotEmpty() && unitSymbol.trim().isNotEmpty() && unitMultiplier.toDoubleOrNull() != null,
                            modifier = Modifier.testTag("confirm_add_unit_button")
                        ) {
                            Text("Create Unit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
