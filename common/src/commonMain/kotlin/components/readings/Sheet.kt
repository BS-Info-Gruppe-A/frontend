package eu.bsinfo.components.readings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Sheet(
    title: String,
    onDismissRequest: () -> Unit,
    state: SheetState = rememberModalBottomSheetState(),
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = state) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = modifier.padding(vertical = 10.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            content()
        }
    }
}

@Composable
fun SelectableRow(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        content = content
    )
}
