package eu.bsinfo.components.readings

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import eu.bsinfo.ReadingsScreenModel
import eu.bsinfo.ReadingsScreenState
import eu.bsinfo.data.Reading
import eu.bsinfo.data.icon
import kotlinx.coroutines.launch

@Composable
fun KindPicker(state: ReadingsScreenState, model: ReadingsScreenModel) {
    if (state.isKindSheetVisible) {
        val scope = rememberCoroutineScope()
        Sheet("Ablesungsart", onDismissRequest = { model.closeKindPickerSheet() }) {
            Reading.Kind.entries.forEach {
                SelectableRow({scope.launch { model.setKind(it) }}) {
                    Icon(it.icon, null)
                    Text(it.humanName, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
