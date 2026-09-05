package com.example

import com.example.data.model.IntegrityStatus
import com.example.data.model.MediaType
import com.example.data.model.RecoveryStatus
import com.example.engine.FileCarver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FileCarverTest {

    @Test
    fun `test JPEG signature detection`() {
        val jpegHeader = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00, 0x10, 0x4A, 0x46)
        val analysis = FileCarver.analyzeHeader(jpegHeader, 1024L)

        assertNotNull(analysis)
        assertEquals(MediaType.PHOTO, analysis?.mediaType)
        assertEquals("image/jpeg", analysis?.mimeType)
        assertTrue(analysis?.signatureName?.contains("JPEG") == true)
    }

    @Test
    fun `test PNG signature detection`() {
        val pngHeader = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D, 0x0A, 0x1A, 0x0A)
        val analysis = FileCarver.analyzeHeader(pngHeader, 2048L)

        assertNotNull(analysis)
        assertEquals(MediaType.PHOTO, analysis?.mediaType)
        assertEquals("image/png", analysis?.mimeType)
    }

    @Test
    fun `test MP4 signature detection`() {
        val mp4Header = byteArrayOf(0x00, 0x00, 0x00, 0x20, 0x66, 0x74, 0x79, 0x70, 0x69, 0x73, 0x6F, 0x6D)
        val analysis = FileCarver.analyzeHeader(mp4Header, 1048576L)

        assertNotNull(analysis)
        assertEquals(MediaType.VIDEO, analysis?.mediaType)
        assertEquals("video/mp4", analysis?.mimeType)
    }

    @Test
    fun `test MP3 ID3 signature detection`() {
        val id3Header = byteArrayOf(0x49, 0x44, 0x33, 0x03, 0x00, 0x00, 0x00, 0x00)
        val analysis = FileCarver.analyzeHeader(id3Header, 500000L)

        assertNotNull(analysis)
        assertEquals(MediaType.AUDIO, analysis?.mediaType)
        assertEquals("audio/mpeg", analysis?.mimeType)
    }

    @Test
    fun `test bytesToHex formatting`() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        val hex = FileCarver.bytesToHex(bytes)
        assertEquals("FF D8 FF", hex)
    }
}
