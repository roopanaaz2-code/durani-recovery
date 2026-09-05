package com.example.engine

import android.graphics.BitmapFactory
import com.example.data.model.IntegrityStatus
import com.example.data.model.MediaType
import com.example.data.model.RecoveryStatus
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

data class SignatureAnalysis(
    val mediaType: MediaType,
    val mimeType: String,
    val signatureName: String,
    val hexSnippet: String,
    val recoveryStatus: RecoveryStatus,
    val integrityStatus: IntegrityStatus,
    val confidenceScore: Int
)

object FileCarver {

    // Magic Byte Arrays
    private val JPEG_MAGIC = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    private val PNG_MAGIC = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte())
    private val PNG_IEND = byteArrayOf(0x49.toByte(), 0x45.toByte(), 0x4E.toByte(), 0x44.toByte(), 0xAE.toByte(), 0x42.toByte(), 0x60.toByte(), 0x82.toByte())
    private val RIFF_MAGIC = byteArrayOf(0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte())
    private val WEBP_MAGIC = byteArrayOf(0x57.toByte(), 0x45.toByte(), 0x42.toByte(), 0x50.toByte())
    private val WAVE_MAGIC = byteArrayOf(0x57.toByte(), 0x41.toByte(), 0x56.toByte(), 0x45.toByte())
    private val FTYP_MAGIC = byteArrayOf(0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte())
    private val MKV_MAGIC = byteArrayOf(0x1A.toByte(), 0x45.toByte(), 0xDF.toByte(), 0xA3.toByte())
    private val ID3_MAGIC = byteArrayOf(0x49.toByte(), 0x44.toByte(), 0x33.toByte())
    private val OGG_MAGIC = byteArrayOf(0x4F.toByte(), 0x67.toByte(), 0x67.toByte(), 0x53.toByte())
    private val FLAC_MAGIC = byteArrayOf(0x66.toByte(), 0x4C.toByte(), 0x61.toByte(), 0x43.toByte())

    fun analyzeHeader(headerBytes: ByteArray, fileLength: Long, testFile: File? = null): SignatureAnalysis? {
        if (headerBytes.size < 4) return null

        val hexSnippet = bytesToHex(headerBytes.take(16).toByteArray())

        // 1. JPEG
        if (startsWith(headerBytes, JPEG_MAGIC)) {
            var integrity = IntegrityStatus.VALID_HEADER_ONLY
            var status = RecoveryStatus.PARTIALLY_RECOVERABLE
            var score = 65

            var hasEof = false
            if (testFile != null && testFile.exists() && testFile.length() > 2) {
                hasEof = checkJpegEof(testFile)
            }

            var canDecode = false
            if (testFile != null && testFile.exists()) {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(testFile.absolutePath, opts)
                if (opts.outWidth > 0 && opts.outHeight > 0) {
                    canDecode = true
                }
            }

            if (canDecode && hasEof) {
                integrity = IntegrityStatus.INTACT
                status = RecoveryStatus.FULLY_RECOVERABLE
                score = 98
            } else if (canDecode) {
                integrity = IntegrityStatus.INTACT
                status = RecoveryStatus.FULLY_RECOVERABLE
                score = 88
            } else if (hasEof) {
                integrity = IntegrityStatus.TRUNCATED
                status = RecoveryStatus.PARTIALLY_RECOVERABLE
                score = 70
            }

            return SignatureAnalysis(
                mediaType = MediaType.PHOTO,
                mimeType = "image/jpeg",
                signatureName = "JPEG/JFIF (FF D8 FF)",
                hexSnippet = hexSnippet,
                recoveryStatus = status,
                integrityStatus = integrity,
                confidenceScore = score
            )
        }

        // 2. PNG
        if (startsWith(headerBytes, PNG_MAGIC)) {
            var hasIend = false
            if (testFile != null && testFile.exists() && testFile.length() >= 12) {
                hasIend = checkPngIend(testFile)
            }

            var canDecode = false
            if (testFile != null && testFile.exists()) {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(testFile.absolutePath, opts)
                if (opts.outWidth > 0 && opts.outHeight > 0) {
                    canDecode = true
                }
            }

            val status = if (canDecode && hasIend) RecoveryStatus.FULLY_RECOVERABLE else if (canDecode) RecoveryStatus.FULLY_RECOVERABLE else RecoveryStatus.PARTIALLY_RECOVERABLE
            val integrity = if (canDecode && hasIend) IntegrityStatus.INTACT else if (canDecode) IntegrityStatus.INTACT else IntegrityStatus.TRUNCATED
            val score = if (canDecode && hasIend) 99 else if (canDecode) 90 else 60

            return SignatureAnalysis(
                mediaType = MediaType.PHOTO,
                mimeType = "image/png",
                signatureName = "PNG Standard (89 50 4E 47)",
                hexSnippet = hexSnippet,
                recoveryStatus = status,
                integrityStatus = integrity,
                confidenceScore = score
            )
        }

        // 3. WebP (RIFF .... WEBP)
        if (headerBytes.size >= 12 && startsWith(headerBytes, RIFF_MAGIC) && matchesAt(headerBytes, 8, WEBP_MAGIC)) {
            return SignatureAnalysis(
                mediaType = MediaType.PHOTO,
                mimeType = "image/webp",
                signatureName = "Google WebP Container (RIFF..WEBP)",
                hexSnippet = hexSnippet,
                recoveryStatus = RecoveryStatus.FULLY_RECOVERABLE,
                integrityStatus = IntegrityStatus.INTACT,
                confidenceScore = 92
            )
        }

        // 4. MP4 / MOV / 3GP / HEIC (ftyp at offset 4)
        if (headerBytes.size >= 12 && matchesAt(headerBytes, 4, FTYP_MAGIC)) {
            val brand = String(headerBytes.sliceArray(8 until 12), Charsets.US_ASCII)
            val isHeic = brand.startsWith("heic") || brand.startsWith("heix") || brand.startsWith("mif1") || brand.startsWith("msf1")
            val isVideo = !isHeic

            return SignatureAnalysis(
                mediaType = if (isHeic) MediaType.PHOTO else MediaType.VIDEO,
                mimeType = if (isHeic) "image/heif" else "video/mp4",
                signatureName = "ISO Base Media File / ftyp '$brand'",
                hexSnippet = hexSnippet,
                recoveryStatus = RecoveryStatus.FULLY_RECOVERABLE,
                integrityStatus = IntegrityStatus.INTACT,
                confidenceScore = 90
            )
        }

        // 5. MKV / WebM
        if (startsWith(headerBytes, MKV_MAGIC)) {
            return SignatureAnalysis(
                mediaType = MediaType.VIDEO,
                mimeType = "video/x-matroska",
                signatureName = "Matroska/EBML Video (1A 45 DF A3)",
                hexSnippet = hexSnippet,
                recoveryStatus = RecoveryStatus.FULLY_RECOVERABLE,
                integrityStatus = IntegrityStatus.INTACT,
                confidenceScore = 90
            )
        }

        // 6. MP3 (ID3v2 or Frame Sync)
        if (startsWith(headerBytes, ID3_MAGIC)) {
            return SignatureAnalysis(
                mediaType = MediaType.AUDIO,
                mimeType = "audio/mpeg",
                signatureName = "MPEG Audio ID3v2 Tag (49 44 33)",
                hexSnippet = hexSnippet,
                recoveryStatus = RecoveryStatus.FULLY_RECOVERABLE,
                integrityStatus = IntegrityStatus.INTACT,
                confidenceScore = 95
            )
        }
        if ((headerBytes[0].toInt() and 0xFF) == 0xFF && (headerBytes[1].toInt() and 0xE0) == 0xE0) {
            return SignatureAnalysis(
                mediaType = MediaType.AUDIO,
                mimeType = "audio/mpeg",
                signatureName = "MPEG Audio Frame Sync (FF Ex)",
                hexSnippet = hexSnippet,
                recoveryStatus = RecoveryStatus.PARTIALLY_RECOVERABLE,
                integrityStatus = IntegrityStatus.VALID_HEADER_ONLY,
                confidenceScore = 75
            )
        }

        // 7. WAV (RIFF .... WAVE)
        if (headerBytes.size >= 12 && startsWith(headerBytes, RIFF_MAGIC) && matchesAt(headerBytes, 8, WAVE_MAGIC)) {
            return SignatureAnalysis(
                mediaType = MediaType.AUDIO,
                mimeType = "audio/wav",
                signatureName = "PCM Wave Audio (RIFF..WAVE)",
                hexSnippet = hexSnippet,
                recoveryStatus = RecoveryStatus.FULLY_RECOVERABLE,
                integrityStatus = IntegrityStatus.INTACT,
                confidenceScore = 95
            )
        }

        // 8. OGG
        if (startsWith(headerBytes, OGG_MAGIC)) {
            return SignatureAnalysis(
                mediaType = MediaType.AUDIO,
                mimeType = "audio/ogg",
                signatureName = "Ogg Bitstream Container (OggS)",
                hexSnippet = hexSnippet,
                recoveryStatus = RecoveryStatus.FULLY_RECOVERABLE,
                integrityStatus = IntegrityStatus.INTACT,
                confidenceScore = 90
            )
        }

        // 9. FLAC
        if (startsWith(headerBytes, FLAC_MAGIC)) {
            return SignatureAnalysis(
                mediaType = MediaType.AUDIO,
                mimeType = "audio/flac",
                signatureName = "Free Lossless Audio Codec (fLaC)",
                hexSnippet = hexSnippet,
                recoveryStatus = RecoveryStatus.FULLY_RECOVERABLE,
                integrityStatus = IntegrityStatus.INTACT,
                confidenceScore = 95
            )
        }

        return null
    }

    private fun checkJpegEof(file: File): Boolean {
        return try {
            java.io.RandomAccessFile(file, "r").use { raf ->
                val len = raf.length()
                if (len < 2) return false
                raf.seek(len - 2)
                val b1 = raf.read()
                val b2 = raf.read()
                b1 == 0xFF && b2 == 0xD9
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun checkPngIend(file: File): Boolean {
        return try {
            java.io.RandomAccessFile(file, "r").use { raf ->
                val len = raf.length()
                if (len < 12) return false
                raf.seek(len - 12)
                val buffer = ByteArray(12)
                raf.readFully(buffer)
                // Search for IEND
                for (i in 0..4) {
                    if (buffer[i] == 0x49.toByte() &&
                        buffer[i + 1] == 0x45.toByte() &&
                        buffer[i + 2] == 0x4E.toByte() &&
                        buffer[i + 3] == 0x44.toByte()) {
                        return true
                    }
                }
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun startsWith(array: ByteArray, prefix: ByteArray): Boolean {
        if (array.size < prefix.size) return false
        for (i in prefix.indices) {
            if (array[i] != prefix[i]) return false
        }
        return true
    }

    private fun matchesAt(array: ByteArray, offset: Int, expected: ByteArray): Boolean {
        if (array.size < offset + expected.size) return false
        for (i in expected.indices) {
            if (array[offset + i] != expected[i]) return false
        }
        return true
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(bytes.size * 3)
        for (b in bytes) {
            val octet = b.toInt() and 0xFF
            result.append(hexChars[octet ushr 4])
            result.append(hexChars[octet and 0x0F])
            result.append(' ')
        }
        return result.toString().trim()
    }

    fun calculateSha256(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            null
        }
    }

    fun calculateSha256(inputStream: InputStream): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            null
        }
    }
}
