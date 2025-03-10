package eu.bsinfo.components.readings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.bsinfo.ReadingsScreenModel
import eu.bsinfo.ReadingsScreenState
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@Composable
fun ReadingDatePicker(state: ReadingsScreenState, model: ReadingsScreenModel) {
    if (state.isDatePickerDialogVisible) {
        val picketState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = state.selectedStartDate?.atStartOfDayIn(TimeZone.currentSystemDefault())
                ?.toEpochMilliseconds(),
            initialSelectedEndDateMillis = state.selectedEndDate?.atStartOfDayIn(TimeZone.currentSystemDefault())
                ?.toEpochMilliseconds(),
        )
        val scope = rememberCoroutineScope()

        DatePickerDialog(
            { model.closeDateSheet() }, confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        model.setDateRange(picketState.selectedStartDateMillis, picketState.selectedEndDateMillis)
                    }
                }, enabled = picketState.selectedStartDateMillis != null) {
                    Text("Ok")
                }
            }, dismissButton = {
                TextButton(onClick = { model.closeDateSheet() }) {
                    Text("Abbrechen")
                }
            }, modifier = Modifier.padding(vertical = 25.dp)
        ) {
            DateRangePicker(picketState)
        }
    }
}
