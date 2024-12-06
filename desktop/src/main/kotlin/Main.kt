package eu.bsinfo.desktop

import androidx.compose.ui.window.singleWindowApplication
import eu.bsinfo.BSInfoApp

fun main() {
    singleWindowApplication(title ="Ganz tolle app") {
        BSInfoApp()
    }
}