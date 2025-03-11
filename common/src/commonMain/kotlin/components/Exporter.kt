package eu.bsinfo.components

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer

@Composable
expect fun rememberExporterState(): ExporterState

expect class ExporterState

@Composable
expect fun ExporterDropdownEntry(state: ExporterState, onClose: () -> Unit)

@Composable
expect fun <T> Exporter(state: ExporterState, items: List<T>, serializer: KSerializer<T>, onClose: () -> Unit)
