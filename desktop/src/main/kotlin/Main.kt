package eu.bsinfo.desktop

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.window.singleWindowApplication
import eu.bsinfo.BSInfoApp
import eu.bsinfo.native_helper.generated.NativeHelper

fun main() {
    singleWindowApplication(title ="Ganz tolle app") {
        // Calling this forces the class loader to load the class and perform the symbol lookup at startup
        // This makes upcoming native calls faster
        SideEffect { NativeHelper.size_t }
        BSInfoApp()
    }
}
