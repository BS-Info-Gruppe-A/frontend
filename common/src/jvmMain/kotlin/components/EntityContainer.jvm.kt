package eu.bsinfo.components

import eu.bsinfo.Loom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.nfd.NFDFilterItem
import org.lwjgl.util.nfd.NativeFileDialog
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.ByteBuffer

actual fun openFile(path: Path) {
    Desktop.getDesktop().browseFileDirectory(File(path.toString()))
}

actual suspend fun openSaveDialog(vararg filters: Filter): Path {
    return if (System.getProperty("os.name").contains("OS X", ignoreCase = true)) {
        openFileDialogAwt()
    } else {
        openFileDialogNative(*filters)
    }
}

private suspend fun openFileDialogAwt(): Path = withContext(Dispatchers.Loom){
    val dialog = FileDialog(null as Frame?, "Select path", FileDialog.SAVE)
    dialog.isVisible = true

    val file = dialog.file ?: throw FileDialogCancelException()
    val dir = dialog.directory ?: throw FileDialogCancelException()
    Path(dir, file)
}

private suspend fun openFileDialogNative(vararg filters: Filter): Path = withContext(Dispatchers.Loom) {
    val output = MemoryStack.stackPush().use {
        println(Thread.currentThread().name)
        val pathPointer = it.mallocPointer(1)
        val nfdFilters = if (filters.isNotEmpty()) NFDFilterItem.malloc(filters.size) else null
        filters.forEachIndexed { index, (name, extension) ->
            nfdFilters!!.get(index)
                .name(it.UTF8(name))
                .spec(it.UTF8(extension))
        }

        when (val response = NativeFileDialog.NFD_SaveDialog(pathPointer, nfdFilters, null as ByteBuffer?, null)) {
            NativeFileDialog.NFD_OKAY -> pathPointer.stringUTF8
            NativeFileDialog.NFD_CANCEL -> throw FileDialogCancelException()
            NativeFileDialog.NFD_ERROR -> throw FileDialogException(NativeFileDialog.NFD_GetError())
            else -> error("Unknown response code: $response")
        }
    }

    Path(output)
}