package eu.bsinfo.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import eu.bsinfo.BSInfoApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow {
        BSInfoApp()
    }
}
