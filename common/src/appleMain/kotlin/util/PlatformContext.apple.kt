package eu.bsinfo.util

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import platform.UIKit.UIViewController

actual abstract class PlatformContext(val viewController: UIViewController)

class PlatformContextImpl(viewController: UIViewController) : PlatformContext(viewController)

actual val LocalPlatformContext: ProvidableCompositionLocal<PlatformContext> =
    staticCompositionLocalOf { error("No default") }
