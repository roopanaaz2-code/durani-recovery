package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.ScanReportEntity
import com.example.ui.components.PreviewForensicDialog
import com.example.ui.components.RecoveryReportDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.SettingsLimitationsScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusPartiallyRecoverable
import com.example.ui.viewmodel.RecoveryViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Scanner", Icons.Default.Search)
    object Results : Screen("results", "Results", Icons.Default.Inventory)
    object History : Screen("history", "Reports", Icons.Default.Assessment)
    object Limitations : Screen("limitations", "Forensic Limits", Icons.Default.Policy)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DuraniRecoveryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuraniRecoveryApp(
    viewModel: RecoveryViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // States from ViewModel
    val storageStats by viewModel.deviceStorageStats.collectAsStateWithLifecycle()
    val scanStats by viewModel.scanStats.collectAsStateWithLifecycle()
    val allCandidates by viewModel.candidates.collectAsStateWithLifecycle()
    val filteredCandidates by viewModel.filteredCandidates.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedCandidateIds.collectAsStateWithLifecycle()
    val scanHistory by viewModel.scanHistory.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedScanMode.collectAsStateWithLifecycle()
    val searchLastTwoYears by viewModel.searchLastTwoYears.collectAsStateWithLifecycle()
    val mediaTypeFilter by viewModel.selectedMediaTypeFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val previewCandidate by viewModel.previewCandidate.collectAsStateWithLifecycle()
    val isRecovering by viewModel.isRecovering.collectAsStateWithLifecycle()
    val recoveryMessage by viewModel.recoveryProgressMessage.collectAsStateWithLifecycle()
    val lastReport by viewModel.lastGeneratedReport.collectAsStateWithLifecycle()

    var activeReportForDialog by remember { mutableStateOf<ScanReportEntity?>(null) }

    // Show report automatically when completed
    LaunchedEffect(lastReport) {
        if (lastReport != null && !scanStats.isRunning && scanStats.isComplete) {
            activeReportForDialog = lastReport
        }
    }

    // Storage Permissions Check
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    var hasPermissions by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results.values.all { it }
        viewModel.refreshStorageInfo()
        if (hasPermissions) {
            scope.launch { snackbarHostState.showSnackbar("Storage access granted. Storage ready for genuine scan.") }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                val items = listOf(
                    Screen.Dashboard,
                    Screen.Results,
                    Screen.History,
                    Screen.Limitations
                )

                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            if (screen == Screen.Results && allCandidates.isNotEmpty()) {
                                BadgedBox(badge = {
                                    Badge(
                                        containerColor = CyanPrimary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(allCandidates.size.toString(), fontSize = 10.sp)
                                    }
                                }) {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            } else {
                                Icon(screen.icon, contentDescription = screen.title)
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanPrimary,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Rationale Banner (if needed)
            if (!hasPermissions) {
                Surface(
                    color = StatusPartiallyRecoverable.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("permission_rationale_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = StatusPartiallyRecoverable,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.storage_permission_rationale),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { permissionLauncher.launch(requiredPermissions) },
                            modifier = Modifier.testTag("grant_permission_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Grant", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Navigation Host
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.weight(1f)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        storageStats = storageStats,
                        scanStats = scanStats,
                        candidates = allCandidates,
                        selectedScanMode = selectedMode,
                        searchLastTwoYears = searchLastTwoYears,
                        onModeSelected = { viewModel.setScanMode(it) },
                        onSearchLastTwoYearsChanged = { viewModel.setSearchLastTwoYears(it) },
                        onStartScan = {
                            if (!hasPermissions) {
                                permissionLauncher.launch(requiredPermissions)
                            } else {
                                viewModel.startScan()
                            }
                        },
                        onCancelScan = { viewModel.cancelScan() },
                        onNavigateToResults = { navController.navigate(Screen.Results.route) },
                        onNavigateToHistory = { navController.navigate(Screen.History.route) }
                    )
                }

                composable(Screen.Results.route) {
                    ResultsScreen(
                        candidates = filteredCandidates,
                        selectedCandidateIds = selectedIds,
                        selectedMediaType = mediaTypeFilter,
                        selectedStatus = statusFilter,
                        searchQuery = searchQuery,
                        isRecovering = isRecovering,
                        recoveryMessage = recoveryMessage,
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                        onMediaTypeSelected = { viewModel.setMediaTypeFilter(it) },
                        onStatusSelected = { viewModel.setStatusFilter(it) },
                        onToggleSelect = { viewModel.toggleCandidateSelection(it) },
                        onSelectAll = { viewModel.selectAllFiltered() },
                        onDeselectAll = { viewModel.deselectAll() },
                        onCandidateClicked = { viewModel.setPreviewCandidate(it) },
                        onRecoverSelected = { viewModel.recoverSelectedCandidates() }
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        reports = scanHistory,
                        onReportClicked = { activeReportForDialog = it },
                        onClearHistory = { viewModel.clearReportHistory() }
                    )
                }

                composable(Screen.Limitations.route) {
                    SettingsLimitationsScreen(storageStats = storageStats)
                }
            }
        }
    }

    // File Forensic Preview Dialog
    previewCandidate?.let { candidate ->
        PreviewForensicDialog(
            candidate = candidate,
            onDismiss = { viewModel.setPreviewCandidate(null) },
            onRecover = { item ->
                viewModel.toggleCandidateSelection(item.id)
                viewModel.recoverSelectedCandidates()
            }
        )
    }

    // Forensic Audit Report Dialog
    activeReportForDialog?.let { report ->
        RecoveryReportDialog(
            report = report,
            onDismiss = { activeReportForDialog = null }
        )
    }
}
