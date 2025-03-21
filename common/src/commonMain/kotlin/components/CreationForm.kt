package eu.bsinfo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.bsinfo.data.ReadableEnum
import eu.bsinfo.util.LocalPlatformContext
import eu.bsinfo.util.formatLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun CreationSheet(
    model: EntityViewModel<*>,
    title: String,
    validate: () -> Boolean,
    onInsert: suspend CoroutineScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(Boolean) -> Unit
) = BoxWithConstraints {
    val state by model.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (state.creationFormVisible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { model.closeCreationForm() },
            sheetMaxWidth = maxWidth * .85f,
            modifier = modifier
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            CreationForm(validate, onInsert, { Text("Erstellen") }, { Icon(Icons.Default.Save, "Save") }, content)
        }
    }
}

@Composable
fun CreationForm(
    validate: () -> Boolean,
    onSave: suspend CoroutineScope.() -> Unit,
    saveButtonIcon: @Composable () -> Unit,
    saveButtonText: @Composable () -> Unit,
    content: @Composable ColumnScope.(Boolean) -> Unit,
) {
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column {
        content(loading)

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp, horizontal = 25.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button({
                loading = true
                scope.launch {
                    if (validate()) {
                        onSave()
                    }
                    loading = false
                }
            }) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize))
                } else {
                    saveButtonIcon()
                }
                saveButtonText()
            }
        }
    }
}

@Composable
fun DatePickerInputField(
    date: Instant,
    setValue: (Instant) -> Unit,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    preSelect: Instant?=null,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    val localDate = date.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val context = LocalPlatformContext.current

    OutlinedTextField(
        formatLocalDate(context, localDate), {}, readOnly = true,
        enabled = enabled,
        singleLine = true,
        isError = isError,
        modifier = modifier,
        supportingText = supportingText,
        trailingIcon = {
            IconButton({ visible = true }) {
                Icon(Icons.Default.CalendarMonth, "Select date")
            }
        })

    if (visible) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = preSelect?.toEpochMilliseconds(),
            selectableDates = selectableDates
        )
        DatePickerDialog(
            { visible = false },
            confirmButton = {
                TextButton(
                    { setValue(Instant.fromEpochMilliseconds(state.selectedDateMillis!!)); visible = false },
                    enabled = state.selectedDateMillis != null
                ) {
                    Text("Bestätigen")
                }
            },
            dismissButton = { TextButton({ visible = false }) { Text("Abbrechen") } }
        ) {
            DatePicker(state)
        }
    }
}

@Composable
inline fun <reified E> EnumInputField(
    current: E?,
    crossinline setValue: (E) -> Unit,
    noinline placeholder: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
) where E : Enum<E>, E : ReadableEnum {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = false }) {
        OutlinedTextField(
            current?.humanName ?: "",
            { },
            placeholder = placeholder,
            enabled = enabled,
            readOnly = true,
            isError = isError,
            singleLine = true,
            trailingIcon = {
                IconButton({
                    expanded = true
                }) { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            },
            modifier = modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            enumValues<E>().forEach {
                DropdownMenuItem(
                    text = { Text(it.humanName) },
                    onClick = {
                        setValue(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun Labeled(
    label: String,
    alignment: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) = Column {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        horizontalAlignment = alignment,
        modifier = modifier
    ) {
        Text(label, style = MaterialTheme.typography.headlineSmall)
        content()
    }
}
