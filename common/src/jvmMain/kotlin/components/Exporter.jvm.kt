package eu.bsinfo.components

import eu.bsinfo.util.FileHandle
import eu.bsinfo.util.Filter
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import java.awt.Desktop

actual val supportsBrowseDirectory: Boolean
    get() = Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)

actual fun browseDirectory(path: FileHandle) = Desktop.getDesktop().browseFileDirectory(path.asFile())
actual fun openFile(path: FileHandle) = Desktop.getDesktop().open(path.asFile())

actual suspend fun writeToFile(path: FileHandle, content: String) =
    SystemFileSystem.sink(path.delegate).buffered().use { it.writeString(content) }

actual suspend fun openSaveDialog(vararg filters: Filter): FileHandle {
    val path = eu.bsinfo.util.openSaveDialog(*filters)
    return FileHandle(path)
}

