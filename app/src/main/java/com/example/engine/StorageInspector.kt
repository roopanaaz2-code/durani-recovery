package com.example.engine

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import com.example.data.model.DeviceStorageStats
import java.io.File

object StorageInspector {

    fun inspectDeviceStorage(context: Context): DeviceStorageStats {
        val internalDataDir = Environment.getDataDirectory()
        val statInternal = try {
            StatFs(internalDataDir.path)
        } catch (_: Exception) {
            null
        }

        val totalInternalBytes = statInternal?.totalBytes ?: 0L
        val freeInternalBytes = statInternal?.availableBytes ?: 0L
        val usedInternalBytes = (totalInternalBytes - freeInternalBytes).coerceAtLeast(0L)

        // Calculate accessible scannable bytes across standard external storage directories
        var accessibleBytes = 0L
        try {
            val externalDir = Environment.getExternalStorageDirectory()
            if (externalDir != null && externalDir.exists()) {
                val statExt = StatFs(externalDir.path)
                accessibleBytes = statExt.totalBytes - statExt.availableBytes
            }
        } catch (_: Exception) {
            accessibleBytes = 0L
        }

        // Removable SD Card detection
        var hasSdCard = false
        var sdTotal = 0L
        var sdFree = 0L

        try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            if (storageManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val volumes = storageManager.storageVolumes
                for (volume in volumes) {
                    if (volume.isRemovable) {
                        hasSdCard = true
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val dir = volume.directory
                            if (dir != null && dir.exists()) {
                                val stat = StatFs(dir.path)
                                sdTotal = stat.totalBytes
                                sdFree = stat.availableBytes
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Error handling for SD card query
        }

        val isScopedStorage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val isRootAvailable = checkRealRoot()

        val fsType = detectFilesystemType(internalDataDir)

        return DeviceStorageStats(
            totalInternalBytes = totalInternalBytes,
            freeInternalBytes = freeInternalBytes,
            usedInternalBytes = usedInternalBytes,
            accessibleScannableBytes = if (accessibleBytes > 0) accessibleBytes else usedInternalBytes,
            hasSdCard = hasSdCard,
            sdCardTotalBytes = sdTotal,
            sdCardFreeBytes = sdFree,
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            apiLevel = Build.VERSION.SDK_INT,
            isScopedStorageActive = isScopedStorage,
            isRootAvailable = isRootAvailable,
            filesystemType = fsType
        )
    }

    private fun checkRealRoot(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            try {
                if (File(path).exists()) return true
            } catch (_: Exception) {
                // Ignore security exceptions
            }
        }
        return false
    }

    private fun detectFilesystemType(dir: File): String {
        return try {
            val mountFile = File("/proc/mounts")
            if (mountFile.exists() && mountFile.canRead()) {
                val lines = mountFile.readLines()
                for (line in lines) {
                    if (line.contains(" /data ") || line.contains(" /data/ ")) {
                        val parts = line.split("\\s+".toRegex())
                        if (parts.size >= 3) {
                            return parts[2] // filesystem type: ext4, f2fs, etc.
                        }
                    }
                }
            }
            "Linux VFS / Ext4 / F2FS (Encrypted)"
        } catch (_: Exception) {
            "Android Storage / Scoped FUSE"
        }
    }
}
