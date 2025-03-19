package eu.bsinfo.components.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.bsinfo.CustomersScreenModel
import eu.bsinfo.components.*
import eu.bsinfo.data.Customer
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.PastDates
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.Uuid

@Composable
fun rememberCustomerUpdateFormState(customer: Customer) = remember(customer) { CustomerCreationFormState(customer) }

@Composable
fun rememberCustomerCreationFormState(key: Any? = null) = remember(key) { CustomerCreationFormState() }

class CustomerCreationFormState(val customer: Customer? = null) {
    val id = customer?.id ?: Uuid.random()
    var firstName by mutableStateOf(customer?.firstName ?: "")
    var firstNameIsError by mutableStateOf(false)
    var lastName by mutableStateOf(customer?.lastName ?: "")
    var lastNameIsError by mutableStateOf(false)
    var date by mutableStateOf(
        customer?.birthDate?.atStartOfDayIn(TimeZone.currentSystemDefault()) ?: Clock.System.now()
    )
    var dateIsError by mutableStateOf(false)
    var gender by mutableStateOf<Customer.Gender?>(customer?.gender)
    var genderIsError by mutableStateOf(false)
    var enabled by mutableStateOf(true)

    val isValid by derivedStateOf { firstName.isNotBlank() && lastName.isNotBlank() && gender != null }

    fun toCustomer() = Customer(
        id,
        firstName,
        lastName,
        date.toLocalDateTime(TimeZone.currentSystemDefault()).date,
        gender!!
    )

    fun validate(): Boolean {
        firstNameIsError = firstName.isBlank()
        lastNameIsError = lastName.isBlank()
        genderIsError = gender == null
        dateIsError = date > Clock.System.now()
        return !firstNameIsError && !lastNameIsError && !genderIsError && !dateIsError
    }
}

@Composable
fun CustomerCreationInput(
    state: CustomerCreationFormState = rememberCustomerCreationFormState(),
    disabled: Boolean = false,
    modifier: Modifier = Modifier
) = Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        Labeled("Vorname") {
            OutlinedTextField(
                state.firstName, { state.firstNameIsError = false; state.firstName = it },
                singleLine = true,
                isError = state.firstNameIsError,
                placeholder = { Text("Max") }, enabled = !disabled && state.enabled
            )
        }
        Labeled("Nachname") {
            OutlinedTextField(
                state.lastName, { state.lastNameIsError = false; state.lastName = it },
                singleLine = true,
                isError = state.lastNameIsError,
                placeholder = { Text("Musterfrau") }, enabled = !disabled && state.enabled
            )
        }
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        Labeled("Geschlecht") {
            EnumInputField(
                state.gender, { state.genderIsError = false; state.gender = it },
                isError = state.genderIsError,
                placeholder = { Text("Bus") }, enabled = !disabled && state.enabled
            )
        }
        Labeled("Geburtsdatum") {
            DatePickerInputField(
                state.date, { state.dateIsError = false; state.date = it },
                isError = state.dateIsError,
                preSelect = state.customer?.birthDate?.atStartOfDayIn(TimeZone.currentSystemDefault()),
                selectableDates = PastDates,
                enabled = !disabled && state.enabled
            )
        }
    }
}

@Composable
fun CustomerCreationSheet(model: CustomersScreenModel) {
    val apiClient = LocalClient.current
    val state = rememberCustomerCreationFormState(model.uiState.value)

    CreationSheet(
        model, "Kunde erstellen",
        onInsert = {
            apiClient.createCustomer(
                Customer(
                    Uuid.random(),
                    state.firstName,
                    state.lastName,
                    state.date.toLocalDateTime(TimeZone.currentSystemDefault()).date,
                    state.gender!!
                )
            )
            model.refresh()
            model.closeCreationForm()
        },
        validate = state::validate,
    ) { loading -> CustomerCreationInput(state, loading) }
}

@Composable
fun CustomerCreationForm(
    state: CustomerCreationFormState,
    saveButtonIcon: @Composable () -> Unit = { Icon(Icons.Default.Save, "Save") },
    saveButtonText: @Composable () -> Unit,
    onSave: suspend CoroutineScope.() -> Unit
) {
    CreationForm(state::validate, onSave, saveButtonIcon, saveButtonText) { loading ->
        CustomerCreationInput(state, loading)
    }
}
