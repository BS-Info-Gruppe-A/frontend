package eu.bsinfo

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import kotlin.time.Duration.Companion.seconds

@Suppress("EXTENSION_SHADOWED_BY_MEMBER") // It doesn't on commonMain
actual val Dispatchers.IO: CoroutineDispatcher get() = IO // This returns the member Dispatchers.IO

@Composable
actual fun isSystemInDarkMode(): Boolean {
    var darkTheme by remember {
        mutableStateOf(currentSystemTheme == SystemTheme.DARK)
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            darkTheme = currentSystemTheme == SystemTheme.DARK
            delay(1.seconds)
        }
    }

    return darkTheme
}

actual val isMobile: Boolean = false
