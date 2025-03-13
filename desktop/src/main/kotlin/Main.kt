package eu.bsinfo.desktop

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import eu.bsinfo.BSInfoApp
import eu.bsinfo.native_helper.generated.NativeHelper
import eu.bsinfo.util.PlatformContextImpl
import eu.bsinfo.util.ProvidePlatformContext

fun main() = application {
    // Calling this forces the class loader to load the class and perform the symbol lookup at startup
    // This makes upcoming native calls faster
    SideEffect { NativeHelper.size_t }
    ProvidePlatformContext(PlatformContextImpl(this@application)) {
        Window(title = "HausFix", onCloseRequest = ::exitApplication) {
            BSInfoApp()
        }
    }
}
