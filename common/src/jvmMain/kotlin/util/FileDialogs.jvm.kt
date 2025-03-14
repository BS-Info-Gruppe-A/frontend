package eu.bsinfo.util

import androidx.compose.runtime.Composable
import eu.bsinfo.Loom
import eu.bsinfo.file_dialog.Filter
import eu.bsinfo.native_helper.openFile
import eu.bsinfo.native_helper.saveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import java.io.File

actual class FileHandle(val delegate: Path) {
    actual val name: String get() = delegate.name
    fun asFile() = File(delegate.toString())
    actual suspend fun read(): String = withContext(Dispatchers.Loom) {
        SystemFileSystem.source(delegate).buffered().use(Source::readString)
    }

    override fun toString(): String = delegate.toString()
}

private object JvmFilePicker : FilePicker {
    override suspend fun chooseFile(vararg filters: Filter): FileHandle =
        FileHandle(openLoadDialog(*filters))
}

@Composable
actual fun rememberFilePicker(): FilePicker = JvmFilePicker

suspend fun openLoadDialog(vararg filters: Filter): Path = withContext(Dispatchers.Loom) {
    Path(openFile(*filters))
}

suspend fun openSaveDialog(vararg filters: Filter): Path = withContext(Dispatchers.Loom) {
    Path(saveFile(*filters))
}
