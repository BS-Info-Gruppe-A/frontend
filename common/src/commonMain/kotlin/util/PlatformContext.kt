package eu.bsinfo.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal

expect abstract class PlatformContext

expect val LocalPlatformContext: ProvidableCompositionLocal<PlatformContext>

@Composable
fun ProvidePlatformContext(context: PlatformContext, content: @Composable () -> Unit) =
    CompositionLocalProvider(LocalPlatformContext provides context, content)