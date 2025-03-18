package eu.bsinfo.ios

import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import eu.bsinfo.BSInfoApp
import eu.bsinfo.util.PlatformContextImpl
import eu.bsinfo.util.ProvidePlatformContext

@Suppress("FunctionName", "unused")
fun MainUIViewController() = ComposeUIViewController {
    val viewController = LocalUIViewController.current
    val context = remember(viewController) { PlatformContextImpl(viewController) }

    ProvidePlatformContext(context) {
        BSInfoApp()
    }
}
