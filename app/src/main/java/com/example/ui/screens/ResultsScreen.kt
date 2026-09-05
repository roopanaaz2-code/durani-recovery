package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaCandidate
import com.example.data.model.MediaType
import com.example.data.model.RecoveryStatus
import com.example.ui.components.MediaCandidateItem
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusCorrupted
import com.example.ui.theme.StatusFragmented
import com.example.ui.theme.StatusFullyRecoverable
import com.example.ui.theme.StatusPartiallyRecoverable

@Composable
fun ResultsScreen(
    candidates: List<MediaCandidate>,
    selectedCandidateIds: Set<String>,
    selectedMediaType: MediaType?,
    selectedStatus: RecoveryStatus?,
    searchQuery: String,
    isRecovering: Boolean,
    recoveryMessage: String,
    onSearchQueryChanged: (String) -> Unit,
    onMediaTypeSelected: (MediaType?) -> Unit,
    onStatusSelected: (RecoveryStatus?) -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onCandidateClicked: (MediaCandidate) -> Unit,
    onRecoverSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("results_screen")
    ) {
        // Top Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_candidates_input"),
                placeholder = { Text("Search by filename or path...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = CyanPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            )
        }

        // Horizontal Filters (Media Type)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedMediaType == null,
                onClick = { onMediaTypeSelected(null) },
                label = { Text("All Media", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                    selectedLabelColor = CyanPrimary
                )
            )
            MediaType.values().forEach { type ->
                FilterChip(
                    selected = selectedMediaType == type,
                    onClick = { onMediaTypeSelected(if (selectedMediaType == type) null else type) },
                    label = { Text(type.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = CyanPrimary
                    )
                )
            }
        }

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatusSelected(null) },
                label = { Text("All Statuses", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                )
            )
            RecoveryStatus.values().forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusSelected(if (selectedStatus == status) null else status) },
                    label = { Text(status.title, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = CyanPrimary
                    )
                )
            }
        }

        // Selection & Results Count Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${candidates.size} verified items",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row {
                TextButton(
                    onClick = onSelectAll,
                    modifier = Modifier.testTag("select_all_button")
                ) {
                    Text("Select All", style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
                }
                if (selectedCandidateIds.isNotEmpty()) {
                    TextButton(
                        onClick = onDeselectAll,
                        modifier = Modifier.testTag("deselect_all_button")
                    ) {
                        Text("Deselect", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Candidates List
        if (candidates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No candidates match the selected filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(candidates, key = { it.id }) { candidate ->
                    MediaCandidateItem(
                        candidate = candidate,
                        isSelected = selectedCandidateIds.contains(candidate.id),
                        onToggleSelect = { onToggleSelect(candidate.id) },
                        onClick = { onCandidateClicked(candidate) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // Bottom Recovery Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (recoveryMessage.isNotEmpty()) {
                    Text(
                        text = recoveryMessage,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = CyanPrimary,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Button(
                    onClick = onRecoverSelected,
                    enabled = selectedCandidateIds.isNotEmpty() && !isRecovering,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("recover_selected_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (isRecovering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("RECOVERING VERIFIED FILES...", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    } else {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedCandidateIds.isEmpty()) "SELECT FILES TO RECOVER" else "RECOVER ${selectedCandidateIds.size} SELECTED FILES",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                    }
                }
            }
        }
    }
}
