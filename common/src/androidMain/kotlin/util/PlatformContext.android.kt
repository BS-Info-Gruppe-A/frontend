package eu.bsinfo.util

import android.content.Context
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.platform.LocalContext

actual typealias PlatformContext = Context

actual inline val LocalPlatformContext: ProvidableCompositionLocal<PlatformContext>
    get() = LocalContext
