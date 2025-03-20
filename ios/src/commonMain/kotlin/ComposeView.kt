package eu.bsinfo.ios

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import eu.bsinfo.BSInfoApp
import eu.bsinfo.data.Client
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.PlatformContextImpl
import eu.bsinfo.util.ProvidePlatformContext

val client = Client()

@Suppress("FunctionName", "unused")
fun MainUIViewController() = ComposeUIViewController {
    val viewController = LocalUIViewController.current
    val context = remember(viewController) { PlatformContextImpl(viewController) }

    CompositionLocalProvider(LocalClient provides client) {
        ProvidePlatformContext(context) {
            BSInfoApp()
        }
    }
}
