package eu.bsinfo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.bsinfo.data.Identifiable
import eu.bsinfo.util.matchingName

@Composable
fun <T : Identifiable> EntityCard(
    entity: T,
    query: String,
    onClick: (() -> Unit)? = null,
    tooltip: @Composable TooltipScope.() -> Unit = {},
    details: @Composable () -> Unit = {}
) {
    val modifier = Modifier
        .width(260.dp)
        .wrapContentHeight()
        .padding(vertical = 7.dp)

    @Composable
    fun Content() {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(Modifier.padding(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                        tooltip = tooltip,
                        state = rememberTooltipState()
                    ) {
                        Text(
                            entity.matchingName(query),
                            style = MaterialTheme.typography.headlineSmall,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
                Row(
                    horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()
                ) {
                    details()
                }
            }
        }
    }

    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier
        ) { Content() }
    } else {
        ElevatedCard(modifier = modifier) { Content() }
    }
}

@Composable
fun Detail(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
        Text(text)
    }
}
