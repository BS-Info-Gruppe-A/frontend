package eu.bsinfo

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
actual fun isSystemInDarkMode() = isSystemInDarkTheme()
