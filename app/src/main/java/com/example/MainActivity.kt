package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.ConverterDatabase
import com.example.data.repository.ConverterRepository
import com.example.ui.components.ConverterScreen
import com.example.ui.components.HistoryScreen
import com.example.ui.components.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ConverterViewModel
import com.example.ui.viewmodel.ConverterViewModelFactory

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize database, repository and ViewModel inside compose cleanly
                val context = LocalContext.current
                val database = remember { ConverterDatabase.getDatabase(context.applicationContext) }
                val repository = remember { ConverterRepository(database.converterDao()) }
                val viewModel: ConverterViewModel = viewModel(
                    factory = ConverterViewModelFactory(repository)
                )

                var activeTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "UniConvert",
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // Green dot signifying fully offline local Room status
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF73F5AF))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Offline Local Database Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (activeTab == 0) Icons.Default.Scale else Icons.Default.Scale,
                                        contentDescription = "Converter"
                                    )
                                },
                                label = { Text("Converter") },
                                modifier = Modifier.testTag("nav_tab_converter")
                            )

                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History Logs"
                                    )
                                },
                                label = { Text("History") },
                                modifier = Modifier.testTag("nav_tab_history")
                            )

                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { activeTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.DashboardCustomize,
                                        contentDescription = "Customize Settings"
                                    )
                                },
                                label = { Text("Customize") },
                                modifier = Modifier.testTag("nav_tab_customize")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            0 -> ConverterScreen(viewModel = viewModel)
                            1 -> HistoryScreen(
                                viewModel = viewModel,
                                onNavigateToConverter = { activeTab = 0 }
                            )
                            2 -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
