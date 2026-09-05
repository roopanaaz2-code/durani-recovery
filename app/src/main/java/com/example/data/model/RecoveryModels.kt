package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MediaType(val label: String) {
    PHOTO("Photo"),
    VIDEO("Video"),
    AUDIO("Audio"),
    RAW_FRAGMENT("Fragment")
}

enum class RecoveryStatus(val title: String, val description: String) {
    FULLY_RECOVERABLE("Fully Recoverable", "Header, metadata, and data stream verified intact."),
    PARTIALLY_RECOVERABLE("Partially Recoverable", "Valid file header found; data stream may be truncated."),
    FRAGMENTED("Fragmented", "Carved binary fragment found in cache/unlinked storage."),
    CORRUPTED("Corrupted", "Corrupt header or unparseable byte stream."),
    METADATA_RECOVERED("Metadata Recovered", "Database record found, but original content is inaccessible."),
    REQUIRES_ADDITIONAL_ACCESS("Requires Additional Access", "File detected in protected storage requiring SAF/root access."),
    NOT_RECOVERABLE("Not Recoverable", "Data has been overwritten, trimmed, or zeroed out.")
}

enum class IntegrityStatus(val label: String) {
    INTACT("Intact"),
    VALID_HEADER_ONLY("Valid Header Only"),
    TRUNCATED("Truncated Stream"),
    CORRUPT_HEADER("Corrupt Header"),
    CHECKSUM_VERIFIED("Checksum Verified"),
    UNKNOWN("Unknown")
}

enum class SourceType(val displayName: String) {
    MEDIASTORE("MediaStore (Active)"),
    MEDIASTORE_TRASHED("MediaStore (Trash/Pending)"),
    CACHE_RESIDUAL("App Cache Remnant"),
    THUMBNAIL_CACHE("Orphaned Thumbnail"),
    STORAGE_CARVED("Storage Signature Carved"),
    EXT_SDCARD("Removable SD Storage")
}

enum class ScanMode(val title: String, val subtitle: String) {
    QUICK("Quick Scan", "Inspects MediaStore database, trash bins, thumbnail caches, and accessible media metadata."),
    DEEP_FORENSIC("Deep Forensic Scan", "Performs binary file carving, header magic byte validation, and storage residual analysis.")
}

data class MediaCandidate(
    val id: String,
    val fileName: String,
    val mediaType: MediaType,
    val mimeType: String,
    val sizeBytes: Long,
    val filePath: String? = null,
    val contentUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val recoveryStatus: RecoveryStatus,
    val integrityStatus: IntegrityStatus,
    val confidenceScore: Int, // 0 to 100
    val signatureDetected: String? = null,
    val headerHexSnippet: String? = null,
    val sha256: String? = null,
    val sourceType: SourceType,
    val isDeletedCandidate: Boolean = false,
    val isRecovered: Boolean = false,
    val recoveredPath: String? = null,
    val recoveryError: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val durationMs: Long = 0
) {
    val formattedSize: String
        get() = formatByteSize(sizeBytes)

    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}

data class ScanStats(
    val currentStage: String = "Idle",
    val currentStorageArea: String = "",
    val bytesExamined: Long = 0L,
    val filesDiscovered: Int = 0,
    val recoverableCandidates: Int = 0,
    val corruptedCandidates: Int = 0,
    val skippedRegionsCount: Int = 0,
    val skippedRegionsDetails: List<String> = emptyList(),
    val scanSpeedBytesPerSec: Long = 0L,
    val elapsedTimeMs: Long = 0L,
    val estimatedRemainingTimeMs: Long? = null,
    val isRunning: Boolean = false,
    val isCancelled: Boolean = false,
    val isComplete: Boolean = false
) {
    val formattedBytesExamined: String
        get() = formatByteSize(bytesExamined)

    val formattedSpeed: String
        get() = "${formatByteSize(scanSpeedBytesPerSec)}/s"
}

data class RecoveryOperationResult(
    val candidateId: String,
    val isSuccess: Boolean,
    val targetPath: String?,
    val verifiedSha256: String?,
    val errorMessage: String? = null
)

data class DeviceStorageStats(
    val totalInternalBytes: Long,
    val freeInternalBytes: Long,
    val usedInternalBytes: Long,
    val accessibleScannableBytes: Long,
    val hasSdCard: Boolean,
    val sdCardTotalBytes: Long = 0L,
    val sdCardFreeBytes: Long = 0L,
    val androidVersion: String,
    val apiLevel: Int,
    val isScopedStorageActive: Boolean,
    val isRootAvailable: Boolean,
    val filesystemType: String
)

fun formatByteSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val groupIndex = digitGroups.coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, groupIndex.toDouble())
    return String.format(Locale.US, "%.1f %s", value, units[groupIndex])
}
