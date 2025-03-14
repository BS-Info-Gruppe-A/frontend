package eu.bsinfo.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import eu.bsinfo.util.AppleFilePicker
import eu.bsinfo.util.ExportJob
import eu.bsinfo.util.FileHandle
import platform.Foundation.NSURL
import platform.UIKit.UIViewController

private class AppleFileSaver(private val controller: UIViewController) : FileSaver {
    override suspend fun saveFile(content: String, filename: String) {
        AppleFilePicker(controller, ExportJob(filename, content)).chooseFile()
    }
}

actual fun FileHandle(filename: String) = FileHandle(NSURL.fileURLWithPath(filename))

@Composable
actual fun rememberFileSaver(): FileSaver {
    val uiController = LocalUIViewController.current

    return remember { AppleFileSaver(uiController) }
}