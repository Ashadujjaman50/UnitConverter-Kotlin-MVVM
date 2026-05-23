package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.ControlScreen
import com.example.ui.components.ConverterScreen
import com.example.ui.components.HistoryScreen
import com.example.ui.components.LanguageResources
import com.example.ui.components.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.ConverterViewModel
import com.example.ui.viewmodel.ConverterViewModelFactory
import com.example.ui.viewmodel.ThemeMode

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Initialize database, repository and ViewModel inside compose cleanly
            val context = LocalContext.current
            val database = remember { ConverterDatabase.getDatabase(context.applicationContext) }
            val repository = remember { ConverterRepository(database.converterDao(), context.applicationContext) }
            val viewModel: ConverterViewModel = viewModel(
                factory = ConverterViewModelFactory(repository)
            )

            val currentTheme by viewModel.themeMode.collectAsState()
            val currentLanguage by viewModel.appLanguage.collectAsState()

            val darkTheme = when (currentTheme) {
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                var activeTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = LanguageResources.getString(currentLanguage, "app_title"),
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
                                            text = LanguageResources.getString(currentLanguage, "offline_status"),
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
                                        imageVector = Icons.Default.Scale,
                                        contentDescription = "Converter"
                                    )
                                },
                                label = { Text(LanguageResources.getString(currentLanguage, "converter")) },
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
                                label = { Text(LanguageResources.getString(currentLanguage, "history")) },
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
                                label = { Text(LanguageResources.getString(currentLanguage, "customize")) },
                                modifier = Modifier.testTag("nav_tab_customize")
                            )

                            NavigationBarItem(
                                selected = activeTab == 3,
                                onClick = { activeTab = 3 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Control Preferences"
                                    )
                                },
                                label = { Text(LanguageResources.getString(currentLanguage, "control")) },
                                modifier = Modifier.testTag("nav_tab_control")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (activeTab) {
                                0 -> ConverterScreen(viewModel = viewModel)
                                1 -> HistoryScreen(
                                    viewModel = viewModel,
                                    onNavigateToConverter = { activeTab = 0 }
                                )
                                2 -> SettingsScreen(viewModel = viewModel)
                                3 -> ControlScreen(viewModel = viewModel)
                            }
                        }

                        // --- Custom Animated Toast Overlay ---
                        // Slides in from Bottom, slides out to Right side.
                        val customToast by viewModel.customToast.collectAsState()
                        
                        LaunchedEffect(customToast) {
                            if (customToast != null) {
                                kotlinx.coroutines.delay(2500)
                                viewModel.clearToast()
                            }
                        }

                        AnimatedVisibility(
                            visible = customToast != null,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) + fadeIn(),
                            exit = slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                            ) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                                .fillMaxWidth()
                                .testTag("custom_toast_container")
                        ) {
                            val msg = customToast?.message ?: ""
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF73F5AF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
