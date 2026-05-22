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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: ConverterViewModel,
    onNavigateToConverter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    var showClearConfirmDialog by remember { mutableStateOf(false) }

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
                text = "Conversion History Logs",
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
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear All Logs")
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
                        text = "Your offline database is clear.",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Any calculations you perform in the calculator can be logged here for offline reference.",
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
                        onItemClick = {
                            viewModel.loadHistoryItem(historyItem)
                            onNavigateToConverter()
                        },
                        onDeleteClick = {
                            viewModel.deleteHistoryItem(historyItem.id)
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
            title = { Text("Clear All Logs?") },
            text = { Text("This will permanently delete all offline historical logs. This action is irreversible.") },
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
                    Text("Delete Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryEntity,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val timeStr = remember(item.timestamp) { sdf.format(Date(item.timestamp)) }

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
                    imageVector = Icons.Default.Timeline,
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
                        text = item.categoryName,
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

            // Quick swipe actions or delete button
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
