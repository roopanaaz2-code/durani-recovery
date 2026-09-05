package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "scan_reports")
data class ScanReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val scanMode: String,
    val durationMs: Long,
    val bytesExamined: Long,
    val totalFound: Int,
    val recoverableCount: Int,
    val corruptedCount: Int,
    val inaccessibleRegionsCount: Int,
    val recoveredCount: Int,
    val failedRecoveryCount: Int,
    val storageAnalyzed: String,
    val summaryDetails: String
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

    val formattedDuration: String
        get() {
            val seconds = durationMs / 1000
            val minutes = seconds / 60
            val remainingSec = seconds % 60
            return if (minutes > 0) "${minutes}m ${remainingSec}s" else "${remainingSec}s"
        }
}
