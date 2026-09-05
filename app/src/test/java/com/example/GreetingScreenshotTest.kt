package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.DeviceStorageStats
import com.example.ui.components.ForensicHeaderCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleStats = DeviceStorageStats(
      totalInternalBytes = 128_000_000_000L,
      freeInternalBytes = 42_000_000_000L,
      usedInternalBytes = 86_000_000_000L,
      accessibleScannableBytes = 64_000_000_000L,
      hasSdCard = false,
      sdCardTotalBytes = 0L,
      sdCardFreeBytes = 0L,
      androidVersion = "14",
      apiLevel = 34,
      isScopedStorageActive = true,
      isRootAvailable = false,
      filesystemType = "f2fs"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        ForensicHeaderCard(storageStats = sampleStats)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
