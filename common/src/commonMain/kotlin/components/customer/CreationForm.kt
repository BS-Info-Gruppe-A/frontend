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
    val apiClient = LocalClient.current

    if (model.uiState.value.creationFormVisible) {
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(Clock.System.now()) }
        var gender by remember { mutableStateOf<Customer.Gender?>(null) }

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
        }) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                Labeled("Vorname") {
                    OutlinedTextField(firstName, { firstName = it }, placeholder = { Text("Max") })
                }
                Labeled("Nachname") {
                    OutlinedTextField(lastName, { lastName = it }, placeholder = { Text("Musterfrau") })
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                Labeled("Geschlecht") {
                    EnumInputField(gender, { gender = it }, placeholder = { Text("Bus") })
                }
                Labeled("Geburtsdatum") {
                    DatePickerInputField(date, { date = it })
                }
            }
        }
    }
}
