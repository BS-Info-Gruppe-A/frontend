package eu.bsinfo.components

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import eu.bsinfo.util.FileHandle
import eu.bsinfo.util.import
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer

private val LOG = KotlinLogging.logger { }

@Composable
fun <T> Importer(
    importPath: FileHandle?,
    importItem: suspend (T) -> Unit,
    serializer: KSerializer<T>,
    onClose: () -> Unit,
    model: EntityViewModel<*>
) {
    var successfulItems by remember(importPath) { mutableStateOf(0) }
    var failedItems by remember(importPath) { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    @Composable
    fun Report() {
        Text("Erfolgreich: $successfulItems. Fehlgeschlagen: $failedItems")
    }

    DataProcessor(
        importPath, serializer, onClose,
        done = { Text("Importvorgang abgeschlossen") },
        doneDescription = { Report() },
        running = { Report() },
        onDone = { scope.launch { model.refresh() } },
        processor = { format, path, kSerializer ->
            val items = path.import(format, kSerializer)
            items.forEach {
                try {
                    importItem(it)
                    successfulItems++
                } catch (e: Exception) {
                    LOG.warn(e) { "Failed to import item" }
                    failedItems++
                }
            }
        }
    )
}
