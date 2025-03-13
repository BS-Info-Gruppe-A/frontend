package eu.bsinfo.components

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer

@Composable
actual fun rememberExporterState(): ExporterState = ExporterState()

actual class ExporterState

@Composable
actual fun ExporterDropdownEntry(state: ExporterState, onClose: () -> Unit) {
}

@Composable
actual fun <T> Exporter(
    state: ExporterState,
    items: List<T>,
    serializer: KSerializer<T>,
    onClose: () -> Unit
) {
}
