package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaCandidate
import com.example.data.model.MediaType
import com.example.data.model.RecoveryStatus
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusCorrupted
import com.example.ui.theme.StatusFragmented
import com.example.ui.theme.StatusFullyRecoverable
import com.example.ui.theme.StatusMetadataOnly
import com.example.ui.theme.StatusPartiallyRecoverable
import com.example.ui.theme.StatusRequiresAccess

@Composable
fun MediaCandidateItem(
    candidate: MediaCandidate,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (candidate.recoveryStatus) {
        RecoveryStatus.FULLY_RECOVERABLE -> StatusFullyRecoverable
        RecoveryStatus.PARTIALLY_RECOVERABLE -> StatusPartiallyRecoverable
        RecoveryStatus.FRAGMENTED -> StatusFragmented
        RecoveryStatus.CORRUPTED -> StatusCorrupted
        RecoveryStatus.METADATA_RECOVERED -> StatusMetadataOnly
        RecoveryStatus.REQUIRES_ADDITIONAL_ACCESS -> StatusRequiresAccess
        RecoveryStatus.NOT_RECOVERABLE -> StatusCorrupted
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("candidate_item_${candidate.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for recovery selection
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = CyanPrimary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("candidate_checkbox_${candidate.id}")
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Thumbnail / Icon Area
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                val imageModel = candidate.contentUri ?: candidate.filePath
                if (candidate.mediaType == MediaType.PHOTO && imageModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = candidate.fileName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp),
                        error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                    )
                } else {
                    val mediaIcon = when (candidate.mediaType) {
                        MediaType.PHOTO -> Icons.Default.Image
                        MediaType.VIDEO -> Icons.Default.Movie
                        MediaType.AUDIO -> Icons.Default.Audiotrack
                        MediaType.RAW_FRAGMENT -> Icons.Default.ReceiptLong
                    }
                    Icon(
                        imageVector = mediaIcon,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Candidate Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = candidate.fileName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (candidate.isRecovered) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Recovered",
                                tint = StatusFullyRecoverable,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Recovered",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusFullyRecoverable
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Size & Source Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${candidate.formattedSize} • ${candidate.mimeType}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${candidate.confidenceScore}% evidence",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Status Pill & Source Tag
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = candidate.recoveryStatus.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            color = statusColor
                        )
                    }

                    // Source Type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = candidate.sourceType.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
