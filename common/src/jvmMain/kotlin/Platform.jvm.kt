package eu.bsinfo

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds

private val LoomDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

@Suppress("EXTENSION_SHADOWED_BY_MEMBER") // It doesn't on commonMain
actual val Dispatchers.IO: CoroutineDispatcher get() = IO // This returns the member Dispatchers.IO

@Suppress("UnusedReceiverParameter")
val Dispatchers.Loom: CoroutineDispatcher
    get() = LoomDispatcher

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
