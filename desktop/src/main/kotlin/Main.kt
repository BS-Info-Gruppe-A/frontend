package eu.bsinfo.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import eu.bsinfo.BSInfoApp
import eu.bsinfo.data.Client
import eu.bsinfo.native_helper.generated.NativeHelper
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.PlatformContextImpl
import eu.bsinfo.util.ProvidePlatformContext
import org.jetbrains.compose.resources.painterResource

fun main() {
    val client = Client()
    application {
        SideEffect {
            // Calling this forces the class loader to load the class and perform the symbol lookup at startup
            // This makes upcoming native calls faster
            NativeHelper.size_t
            Runtime.getRuntime().addShutdownHook(Thread(Runnable(client::close)))
        }
        CompositionLocalProvider(LocalClient provides client) {
            ProvidePlatformContext(PlatformContextImpl(this@application)) {
                Window(title = "HausFix", icon = painterResource(Res.drawable.logo), onCloseRequest = ::exitApplication) {
                    BSInfoApp()
                }
            }
        }
    }
}
