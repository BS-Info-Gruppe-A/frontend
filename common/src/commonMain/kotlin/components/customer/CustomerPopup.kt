package eu.bsinfo.components.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import eu.bsinfo.CustomersScreenModel
import eu.bsinfo.components.DeleteDialog
import eu.bsinfo.components.Detail
import eu.bsinfo.data.Customer
import eu.bsinfo.util.LocalPlatformContext
import eu.bsinfo.util.formatLocalDate

@Composable
fun CustomerPopup(focusedCustomer: Customer?, model: CustomersScreenModel) {
    val context = LocalPlatformContext.current
    var isDeleteDialogPresent by remember(focusedCustomer) { mutableStateOf(false) }

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
                Text(
                    focusedCustomer.fullName, style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(.5f).fillMaxHeight()
                    ) {
                        Text(
                            "Daten",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                        )
                        Column {
                            Detail(
                                Icons.Filled.Person,
                                focusedCustomer.gender.humanName
                            )
                            Detail(
                                icon = Icons.Filled.Cake,
                                text = formatLocalDate(context, focusedCustomer.birthDate)
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        ) {
                            Button({ isDeleteDialogPresent = true }) {
                                Icon(Icons.Filled.Delete, "Löschen")
                                Text("Löschen")
                            }
                            Button({}) {
                                Icon(Icons.Filled.Edit, "Bearbeiten")
                                Text("Bearbeiten")
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        Text(
                            "Readings",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                        )
                        ReadingList(focusedCustomer.id)
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
