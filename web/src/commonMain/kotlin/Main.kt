package eu.bsinfo.web

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import eu.bsinfo.BSInfoApp
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow {
        SideEffect { document.getElementById("loading-container")!!.remove() }
        BSInfoApp()
    }
}
