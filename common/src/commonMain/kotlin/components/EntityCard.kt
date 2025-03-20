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
        .height(100.dp)
        .padding(vertical = 7.dp)

    @Composable
    fun Content() {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(Modifier.padding(top = 3.dp, bottom = 10.dp)) {
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
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .fillMaxWidth(fraction = .9f)
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
    Row(modifier.padding(horizontal = 3.dp)) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
        Text(text)
    }
}
