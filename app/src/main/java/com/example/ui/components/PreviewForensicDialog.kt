package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaCandidate
import com.example.data.model.MediaType
import com.example.data.model.RecoveryStatus
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusCorrupted
import com.example.ui.theme.StatusFullyRecoverable
import com.example.ui.theme.StatusPartiallyRecoverable
import java.io.File

@Composable
fun PreviewForensicDialog(
    candidate: MediaCandidate,
    onDismiss: () -> Unit,
    onRecover: (MediaCandidate) -> Unit
) {
    val context = LocalContext.current
    var isAudioPlaying by remember { mutableStateOf(false) }
    var audioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var audioPlayError by remember { mutableStateOf<String?>(null) }

    // Clean up media player when dialog closes
    DisposableEffect(candidate) {
        onDispose {
            try {
                audioPlayer?.stop()
                audioPlayer?.release()
                audioPlayer = null
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp)
                .testTag("preview_forensic_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CyanPrimary.copy(alpha = 0.4f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FILE FORENSIC PREVIEW",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = CyanPrimary
                        )
                        Text(
                            text = candidate.fileName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_preview_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PREVIEW SECTION (Real Decoded Data Only)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val mediaSource = candidate.contentUri ?: candidate.filePath

                    when {
                        // Image Preview
                        candidate.mediaType == MediaType.PHOTO && mediaSource != null -> {
                            var imageLoadFailed by remember { mutableStateOf(false) }

                            if (!imageLoadFailed) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(mediaSource)
                                        .listener(onError = { _, _ -> imageLoadFailed = true })
                                        .build(),
                                    contentDescription = candidate.fileName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(200.dp)
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = StatusCorrupted,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Preview unavailable — file may be corrupted or incomplete.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusCorrupted
                                    )
                                }
                            }
                        }

                        // Audio Preview
                        candidate.mediaType == MediaType.AUDIO && mediaSource != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        try {
                                            if (isAudioPlaying) {
                                                audioPlayer?.pause()
                                                isAudioPlaying = false
                                            } else {
                                                if (audioPlayer == null) {
                                                    val player = MediaPlayer()
                                                    if (candidate.contentUri != null) {
                                                        player.setDataSource(context, Uri.parse(candidate.contentUri))
                                                    } else if (candidate.filePath != null) {
                                                        player.setDataSource(candidate.filePath)
                                                    }
                                                    player.prepare()
                                                    player.setOnCompletionListener { isAudioPlaying = false }
                                                    audioPlayer = player
                                                }
                                                audioPlayer?.start()
                                                isAudioPlaying = true
                                                audioPlayError = null
                                            }
                                        } catch (e: Exception) {
                                            isAudioPlaying = false
                                            audioPlayError = "Preview unavailable — file may be corrupted or incomplete."
                                        }
                                    },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                                ) {
                                    Icon(
                                        imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isAudioPlaying) "Pause" else "Play"
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isAudioPlaying) "Pause Audio Stream" else "Play Audio Stream")
                                }

                                if (audioPlayError != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = audioPlayError ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusCorrupted
                                    )
                                }
                            }
                        }

                        // Video / Fragment Preview
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Binary Media Candidate Verified",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Direct stream available for extraction to device storage.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FORENSIC DETAILS TABLE
                Text(
                    text = "FORENSIC ANALYSIS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = CyanPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                ForensicDetailRow("Status", candidate.recoveryStatus.title)
                ForensicDetailRow("Integrity", candidate.integrityStatus.label)
                ForensicDetailRow("Detected Signature", candidate.signatureDetected ?: "N/A")
                ForensicDetailRow("Header Magic Bytes", candidate.headerHexSnippet ?: "N/A", isMonospace = true)
                ForensicDetailRow("File Size", candidate.formattedSize)
                ForensicDetailRow("MIME Type", candidate.mimeType)
                ForensicDetailRow("Discovered Date", candidate.formattedDate)
                ForensicDetailRow("Location", candidate.filePath ?: candidate.contentUri ?: "Unknown", isMonospace = true)

                Spacer(modifier = Modifier.height(12.dp))

                // Status Assessment Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Assessment: ${candidate.recoveryStatus.description}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button: Recover This Item
                Button(
                    onClick = {
                        onRecover(candidate)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("recover_single_item_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECOVER THIS FILE TO STORAGE",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ForensicDetailRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                fontSize = if (isMonospace) 11.sp else 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            modifier = Modifier.weight(1f)
        )
    }
}
