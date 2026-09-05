package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceStorageStats
import com.example.data.model.MediaCandidate
import com.example.data.model.ScanMode
import com.example.data.model.ScanStats
import com.example.ui.components.ActiveScanProgressCard
import com.example.ui.components.ForensicHeaderCard
import com.example.ui.components.ScanControlCard
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusCorrupted
import com.example.ui.theme.StatusFullyRecoverable

@Composable
fun DashboardScreen(
    storageStats: DeviceStorageStats?,
    scanStats: ScanStats,
    candidates: List<MediaCandidate>,
    selectedScanMode: ScanMode,
    searchLastTwoYears: Boolean,
    onModeSelected: (ScanMode) -> Unit,
    onSearchLastTwoYearsChanged: (Boolean) -> Unit,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("dashboard_screen")
    ) {
        // Device Storage & Specs Header
        ForensicHeaderCard(storageStats = storageStats)

        Spacer(modifier = Modifier.height(14.dp))

        // Mandatory Technical Transparency Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(StatusCorrupted.copy(alpha = 0.08f))
                .border(1.dp, StatusCorrupted.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Limitation warning",
                    tint = StatusCorrupted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Real Recovery Engine: Results are strictly verified from device storage. Overwritten or trimmed storage blocks cannot be recovered.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ACTIVE SCAN OR SCAN CONTROL
        if (scanStats.isRunning) {
            ActiveScanProgressCard(
                scanStats = scanStats,
                onCancelScan = onCancelScan
            )
        } else {
            ScanControlCard(
                selectedMode = selectedScanMode,
                onModeSelected = onModeSelected,
                searchLastTwoYears = searchLastTwoYears,
                onSearchLastTwoYearsChanged = onSearchLastTwoYearsChanged,
                onStartScan = onStartScan,
                isScanning = scanStats.isRunning
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // RECENT SCAN CANDIDATES SUMMARY (if any scanned)
        if (candidates.isNotEmpty()) {
            val recoverableCount = candidates.count {
                it.recoveryStatus == com.example.data.model.RecoveryStatus.FULLY_RECOVERABLE ||
                        it.recoveryStatus == com.example.data.model.RecoveryStatus.PARTIALLY_RECOVERABLE
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recent_scan_summary_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StatusFullyRecoverable.copy(alpha = 0.35f))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusFullyRecoverable,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scan Findings Available",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${candidates.size} items",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Found $recoverableCount verified recoverable candidates and ${candidates.size - recoverableCount} corrupted/metadata entries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToResults,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("view_results_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text("VIEW & RECOVER RESULTS", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Action Quick Links: Scan History
        OutlinedButton(
            onClick = onNavigateToHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("view_history_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp), tint = CyanPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Scan Reports History", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
