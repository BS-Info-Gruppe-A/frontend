/*
package eu.bsinfo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.bsinfo.util.export
import eu.bsinfo.util.formats
import eu.bsinfo.util.import
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat

data class Filter(val name: String, val extension: String)

class FileDialogException(message: String?) : RuntimeException(message)
class FileDialogCancelException : RuntimeException()

expect val supportsBrowseDirectory: Boolean
expect suspend fun openSaveDialog(vararg filters: Filter): Path
expect suspend fun openLoadDialog(vararg filters: Filter): Path
expect fun openFile(path: Path)
expect fun browseDirectory(path: Path)

private val LOG = KotlinLogging.logger { }

@Composable
actual fun <T> HamburgerItems(
    importItem: suspend (T) -> Unit,
    onClose: () -> Unit,
    items: List<T>,
    serializer: KSerializer<T>,
    model: EntityViewModel
) {
    val scope = rememberCoroutineScope()

    var exportPath by remember { mutableStateOf<Path?>(null) }
    var importPath by remember { mutableStateOf<Path?>(null) }

    DropdownMenuItem({ Text("Import") }, {
        scope.launch {
            try {
                importPath = openLoadDialog(*formats.keys.map {
                    Filter(it, it)
                }.toTypedArray())
            } catch (_: FileDialogCancelException) {
                onClose()
            }
        }
    }, leadingIcon = { Icon(Icons.AutoMirrored.Default.Logout, "export") })

    Exporter(exportPath, items, serializer) { exportPath = null; onClose() }
    Importer(importPath, importItem, serializer, { importPath = null; onClose() }, model)
}


*/
