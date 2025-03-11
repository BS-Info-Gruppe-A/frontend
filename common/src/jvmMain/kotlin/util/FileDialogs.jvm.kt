package eu.bsinfo.util

import eu.bsinfo.Loom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.nfd.NFDFilterItem
import org.lwjgl.util.nfd.NativeFileDialog
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.ByteBuffer

actual class FileHandle(val delegate: Path) {
    actual val name: String get() = delegate.name
    fun asFile() = File(delegate.toString())
    actual suspend fun read(): String = withContext(Dispatchers.Loom) {
        SystemFileSystem.source(delegate).buffered().use(Source::readString)
    }

    override fun toString(): String = delegate.toString()
}

actual suspend fun chooseFile(vararg filters: Filter): FileHandle? {
    val path = openLoadDialog(*filters)
    return FileHandle(path)
}

suspend fun openLoadDialog(vararg filters: Filter): Path {
    return if (System.getProperty("os.name").contains("OS X", ignoreCase = true)) {
        openFileDialogAwt(FileDialog.LOAD)
    } else {
        openFileDialogNative({ output, nfdFilters ->
            NativeFileDialog.NFD_OpenDialog(
                output,
                nfdFilters,
                null as ByteBuffer?
            )
        }, *filters)
    }
}

suspend fun openSaveDialog(vararg filters: Filter): Path {
    return if (System.getProperty("os.name").contains("OS X", ignoreCase = true)) {
        openFileDialogAwt(FileDialog.SAVE)
    } else {
        openFileDialogNative({ output, nfdFilters ->
            NativeFileDialog.NFD_SaveDialog(
                output,
                nfdFilters,
                null as ByteBuffer?,
                null
            )
        }, *filters)
    }
}

private suspend fun openFileDialogAwt(mode: Int): Path = withContext(Dispatchers.Loom) {
    val dialog = FileDialog(null as Frame?, "Select path", mode)
    dialog.isVisible = true

    val file = dialog.file ?: throw FileDialogCancelException()
    val dir = dialog.directory ?: throw FileDialogCancelException()
    Path(dir, file)
}

private suspend fun openFileDialogNative(
    function: (PointerBuffer, NFDFilterItem.Buffer?) -> Int,
    vararg filters: Filter
): Path = withContext(Dispatchers.Loom) {
    val output = try {
        NativeFileDialog.NFD_Init()
        MemoryStack.stackPush().use {
            val pathPointer = it.mallocPointer(1)
            val nfdFilters = if (filters.isNotEmpty()) NFDFilterItem.malloc(filters.size) else null
            filters.forEachIndexed { index, (name, extension) ->
                nfdFilters!!.get(index)
                    .name(it.UTF8(name))
                    .spec(it.UTF8(extension))
            }

            when (val response = function(pathPointer, nfdFilters)) {
                NativeFileDialog.NFD_OKAY -> pathPointer.stringUTF8
                NativeFileDialog.NFD_CANCEL -> throw FileDialogCancelException()
                NativeFileDialog.NFD_ERROR -> throw FileDialogException(NativeFileDialog.NFD_GetError())
                else -> error("Unknown response code: $response")
            }
        }
    } finally {
        NativeFileDialog.NFD_Quit()
    }

    Path(output)
}
