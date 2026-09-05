package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.DeviceStorageStats
import com.example.ui.components.TechnicalDisclaimerSheet

@Composable
fun SettingsLimitationsScreen(
    storageStats: DeviceStorageStats?,
    modifier: Modifier = Modifier
) {
    TechnicalDisclaimerSheet(
        storageStats = storageStats,
        modifier = modifier.fillMaxSize()
    )
}
