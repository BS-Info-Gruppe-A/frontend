package eu.bsinfo.web

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import eu.bsinfo.BSInfoApp
import eu.bsinfo.data.Client
import eu.bsinfo.rest.LocalClient
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val client = Client()
    CanvasBasedWindow {
        SideEffect {
            window.onbeforeunload = { client.close(); null }
            document.getElementById("loading-container")!!.remove()
        }

        CompositionLocalProvider(LocalClient provides client) {
            BSInfoApp()
        }
    }
}
