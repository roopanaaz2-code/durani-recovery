package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceStorageStats
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusCorrupted
import com.example.ui.theme.StatusPartiallyRecoverable

@Composable
fun TechnicalDisclaimerSheet(
    storageStats: DeviceStorageStats?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("technical_disclaimer_sheet")
    ) {
        // Mandatory Transparency Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = StatusCorrupted.copy(alpha = 0.08f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(StatusCorrupted.copy(alpha = 0.4f))
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Crucial Notice",
                    tint = StatusCorrupted,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CORE FORENSIC PRINCIPLE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = StatusCorrupted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No Android application can guarantee recovery of every deleted file. Deleted data may become permanently unrecoverable when storage blocks are overwritten, trimmed, garbage-collected, encrypted, or otherwise inaccessible.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "WHY DATA BECOMES UNRECOVERABLE ON MODERN ANDROID",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = CyanPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        ForensicExplanationCard(
            icon = Icons.Default.Memory,
            title = "1. Flash Storage TRIM & F2FS Garbage Collection",
            explanation = "Modern eMMC and UFS storage chips run active background TRIM commands (fstrim). Once blocks are trimmed or reclaimed by flash controller wear-leveling algorithms, the raw electrical charges are wiped and return zeroed bytes (0x00)."
        )

        Spacer(modifier = Modifier.height(8.dp))

        ForensicExplanationCard(
            icon = Icons.Default.Lock,
            title = "2. File-Based Encryption (FBE)",
            explanation = "Android devices (Android 10+) employ hardware-backed File-Based Encryption (FBE). When a file is permanently unlinked, its encryption keys (stored in TEE/Keystore) are purged. Even if raw magnetic or NAND traces exist, the bytes are cryptographically unintelligible."
        )

        Spacer(modifier = Modifier.height(8.dp))

        ForensicExplanationCard(
            icon = Icons.Default.Security,
            title = "3. Scoped Storage & Sandbox Architecture",
            explanation = "Android enforces strict app-specific isolation. Without root privileges or specialized user-directed SAF selection, an app cannot read raw Linux block devices (/dev/block/...) or other apps' private directories."
        )

        Spacer(modifier = Modifier.height(8.dp))

        ForensicExplanationCard(
            icon = Icons.Default.Info,
            title = "4. Overwrite Dynamics & 2-Year Reality",
            explanation = "As the device takes new photos, downloads videos, or updates apps, free sectors are continuously re-allocated and overwritten. Files deleted 2 years ago can only be recovered if residual remnants, orphaned thumbnails, or un-reclaimed caches happen to have survived unwritten."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current Device Real Storage Specs
        if (storageStats != null) {
            Text(
                text = "CURRENT DEVICE ENVIRONMENT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = CyanPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SpecLine("Android OS Release", "${storageStats.androidVersion} (API Level ${storageStats.apiLevel})")
                    SpecLine("Active Filesystem", storageStats.filesystemType)
                    SpecLine("Storage Architecture", if (storageStats.isScopedStorageActive) "Scoped Storage (FUSE)" else "Legacy Direct Storage")
                    SpecLine("Superuser Access", if (storageStats.isRootAvailable) "Root detected (Raw carving permitted)" else "Root unavailable (Standard sandbox)")
                    SpecLine("Removable SD Card", if (storageStats.hasSdCard) "Detected & Supported" else "No external SD volume detected")
                }
            }
        }
    }
}

@Composable
private fun ForensicExplanationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    explanation: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpecLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}
