package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CyanPrimary,
    onPrimary = DeepNavy,
    primaryContainer = CyanPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = CyanSecondary,
    onSecondary = DeepNavy,
    background = DeepNavy,
    onBackground = Color(0xFFF1F5F9),
    surface = SurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = BorderDark,
    error = StatusCorrupted,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CyanLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1),
    error = StatusCorrupted,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Can prioritize forensic dark aesthetic or dynamic colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> DarkColorScheme // Forensic recovery apps look best in dark cyber theme by default!
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

