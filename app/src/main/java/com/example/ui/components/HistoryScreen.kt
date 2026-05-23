package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entities.HistoryEntity
import com.example.ui.viewmodel.ConverterViewModel
import com.example.ui.viewmodel.AppLanguage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: ConverterViewModel,
    onNavigateToConverter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<HistoryEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header with Clear All Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LanguageResources.getString(appLanguage, "history_title"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (historyList.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirmDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = LanguageResources.getString(appLanguage, "delete_all_btn"))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = "Empty History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = LanguageResources.getString(appLanguage, "history_empty_title"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = LanguageResources.getString(appLanguage, "history_empty_desc"),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { historyItem ->
                    HistoryItemCard(
                        item = historyItem,
                        appLanguage = appLanguage,
                        onItemClick = {
                            if (historyItem.categoryId != "AGE_CALC") {
                                viewModel.loadHistoryItem(historyItem)
                                onNavigateToConverter()
                            }
                        },
                        onDeleteClick = {
                            itemToDelete = historyItem
                        }
                    )
                }
            }
        }
    }

    // --- CLEAR HISTORY CONFIRMATION DIALOG ---
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(LanguageResources.getString(appLanguage, "clear_all_confirm")) },
            text = { Text(LanguageResources.getString(appLanguage, "clear_all_desc")) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(LanguageResources.getString(appLanguage, "delete_all_btn"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(LanguageResources.getString(appLanguage, "cancel"))
                }
            }
        )
    }

    // --- INDIVIDUAL HISTORY ITEM DELETE CONFIRMATION DIALOG ---
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(LanguageResources.getString(appLanguage, "delete_log")) },
            text = { Text(LanguageResources.getString(appLanguage, "delete_log_confirm")) },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteHistoryItem(it.id) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(LanguageResources.getString(appLanguage, "delete_btn"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(LanguageResources.getString(appLanguage, "cancel"))
                }
            }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryEntity,
    appLanguage: AppLanguage,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val timeStr = remember(item.timestamp) { sdf.format(Date(item.timestamp)) }
    val localizedCategoryName = remember(item.categoryId, item.categoryName, appLanguage) {
        val key = "cat_${item.categoryId.lowercase()}"
        val localized = LanguageResources.getString(appLanguage, key)
        if (localized.isNotEmpty()) localized else item.categoryName
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onItemClick() }
            .testTag("history_item_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon background circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.categoryId) {
                        "LENGTH" -> Icons.Default.Straighten
                        "WEIGHT" -> Icons.Default.FitnessCenter
                        "TEMPERATURE" -> Icons.Default.Thermostat
                        "VOLUME" -> Icons.Default.LocalDrink
                        "AREA" -> Icons.Default.GridView
                        "TIME" -> Icons.Default.Schedule
                        "SPEED" -> Icons.Default.Speed
                        "PRESSURE" -> Icons.Default.Air
                        "FORCE" -> Icons.Default.FitnessCenter
                        "WORK" -> Icons.Default.FlashOn
                        "POWER" -> Icons.Default.Bolt
                        "DATA" -> Icons.Default.Storage
                        "TORQUE" -> Icons.Default.Build
                        "AGE_CALC" -> Icons.Default.Cake
                        else -> Icons.Default.Timeline
                    },
                    contentDescription = "Measurement category symbol",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Conversion content details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = localizedCategoryName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "• $timeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (item.categoryId == "AGE_CALC") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = item.fromUnitName,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.toUnitName,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formatDouble(item.fromValue),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = " " + item.fromUnitName.substringAfterLast("(").replace(")", ""),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Converted to",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(16.dp)
                        )

                        Text(
                            text = formatDouble(item.toValue),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " " + item.toUnitName.substringAfterLast("(").replace(")", ""),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.testTag("delete_log_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete this conversion log",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatDouble(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        // Keep up to 4 decimal places
        val formatted = String.format(Locale.US, "%.4f", value)
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
