package eu.bsinfo.util

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ApplicationScope

actual abstract class PlatformContext(val applicationScope: ApplicationScope)

class PlatformContextImpl(applicationScope: ApplicationScope) : PlatformContext(applicationScope)

actual val LocalPlatformContext: ProvidableCompositionLocal<PlatformContext> =
    staticCompositionLocalOf { error("No default") }
