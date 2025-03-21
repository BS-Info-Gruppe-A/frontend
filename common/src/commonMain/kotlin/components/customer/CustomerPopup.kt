package eu.bsinfo.components.customer

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import eu.bsinfo.CustomersScreenModel
import eu.bsinfo.components.AdaptiveDivider
import eu.bsinfo.components.AdaptiveLayout
import eu.bsinfo.components.DeleteDialog
import eu.bsinfo.components.Detail
import eu.bsinfo.data.Customer
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.LocalPlatformContext
import eu.bsinfo.util.formatLocalDate
import kotlinx.coroutines.launch

@Composable
fun CustomerPopup(focusedCustomer: Customer?, model: CustomersScreenModel) {
    var isDeleteDialogPresent by remember(focusedCustomer) { mutableStateOf(false) }
    var isEditMode by remember(focusedCustomer) { mutableStateOf(false) }

    if (focusedCustomer != null) {
        BasicAlertDialog(
            { model.unfocusEntity() }, properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(.8f)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .fillMaxWidth()
                    .fillMaxHeight(.8f)
                    .shadow(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                AnimatedContent(isEditMode) {
                    if (it) {
                        EditForm(focusedCustomer, model) { isEditMode = false }
                    } else {
                        CustomerDetails(model, focusedCustomer, { isEditMode = true }, { isDeleteDialogPresent = true })
                    }
                }
            }
        }

        DeleteDialog(
            isDeleteDialogPresent,
            { isDeleteDialogPresent = false },
            { model.deleteCustomer(focusedCustomer.id) },
            title = { Text("Kunden löschen?", color = MaterialTheme.colorScheme.onError) },
            text = { Text("Möchten Sie den Kunden wirklich löschen?", color = MaterialTheme.colorScheme.onError) },
        )
    }
}

@Composable
private fun EditForm(customer: Customer, model: CustomersScreenModel, close: () -> Unit) = Column {
    val state = rememberCustomerUpdateFormState(customer)
    val scope = rememberCoroutineScope()
    val client = LocalClient.current

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        IconButton(close) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, "Close")
        }

        Text("${customer.fullName} bearbeiten")

        Spacer(Modifier.weight(1f))
    }

    CustomerCreationForm(state, saveButtonText = { Text("Speichern") }) {
        scope.launch {
            model.updateCustomer(client, state)
        }
    }
}

@Composable
private fun CustomerDetails(model: CustomersScreenModel, customer: Customer, openEditMode: () -> Unit, openDeleteMode: () -> Unit) {
    val context = LocalPlatformContext.current

    Column {
        Text(
            customer.fullName, style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        AdaptiveLayout(
            arrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxPrimary(adaptive(landscape = .5f, portrait = .3f)).fillMaxSecondary()
            ) {
                Text(
                    "Daten",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
                Column {
                    Detail(
                        Icons.Filled.Person,
                        customer.gender.humanName
                    )
                    Detail(
                        icon = Icons.Filled.Cake,
                        text = formatLocalDate(context, customer.birthDate)
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                ) {
                    Button(openDeleteMode) {
                        Icon(Icons.Filled.Delete, "Löschen")
                        Text("Löschen")
                    }
                    Button(openEditMode) {
                        Icon(Icons.Filled.Edit, "Bearbeiten")
                        Text("Bearbeiten")
                    }
                }
            }
            AdaptiveDivider(Modifier.fillMaxSecondary().padding(10.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    "Readings",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
                ReadingList(model, customer)
            }
        }
    }
}
