package eu.bsinfo.components.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.bsinfo.CustomersScreenModel
import eu.bsinfo.components.CreationForm
import eu.bsinfo.components.DatePickerInputField
import eu.bsinfo.components.EnumInputField
import eu.bsinfo.components.Labeled
import eu.bsinfo.data.Customer
import eu.bsinfo.rest.LocalClient
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.Uuid

@Composable
fun CustomerCreationForm(model: CustomersScreenModel) {
    val state by model.uiState.collectAsState()
    var firstName by remember(state) { mutableStateOf("") }
    var firstNameIsError by remember(state) { mutableStateOf(false) }
    var lastName by remember(state) { mutableStateOf("") }
    var lastNameIsError by remember(state) { mutableStateOf(false) }
    var date by remember(state) { mutableStateOf(Clock.System.now()) }
    var dateIsError by remember(state) { mutableStateOf(false) }
    var gender by remember(state) { mutableStateOf<Customer.Gender?>(null) }
    var genderIsError by remember(state) { mutableStateOf(false) }

    val apiClient = LocalClient.current

    if (state.creationFormVisible) {
        CreationForm(model, "Kunde erstellen", {
            apiClient.createCustomer(Customer(
                Uuid.random(),
                firstName,
                lastName,
                date.toLocalDateTime(TimeZone.currentSystemDefault()).date,
                gender!!
            ))
            model.refresh()
            model.closeCreationForm()
        }) { loading ->
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                Labeled("Vorname") {
                    OutlinedTextField(firstName, { firstName = it },
                        placeholder = { Text("Max") }, enabled = !loading)
                }
                Labeled("Nachname") {
                    OutlinedTextField(lastName, { lastName = it },
                        placeholder = { Text("Musterfrau") }, enabled = !loading)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                Labeled("Geschlecht") {
                    EnumInputField(gender, { gender = it },
                        placeholder = { Text("Bus") }, enabled = !loading)
                }
                Labeled("Geburtsdatum") {
                    DatePickerInputField(date, { date = it },  enabled = !loading)
                }
            }
        }
    }
}
