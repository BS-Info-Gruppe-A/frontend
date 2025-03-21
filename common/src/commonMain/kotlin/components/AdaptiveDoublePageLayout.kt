package eu.bsinfo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val BoxWithConstraintsScope.isLandscape: Boolean
    get() = maxWidth > maxHeight

interface AdaptiveLayoutScope : BoxWithConstraintsScope {
    val BoxWithConstraintsScope.isLandscape: Boolean
        get() = maxWidth > maxHeight

    fun Modifier.fillMaxPrimary(fraction: Float = 1f): Modifier =
        if (!isLandscape) fillMaxHeight(fraction) else fillMaxWidth(fraction)

    fun Modifier.fillMaxSecondary(fraction: Float = 1f): Modifier =
        if (!isLandscape) fillMaxWidth(fraction) else fillMaxHeight(fraction)

    fun Modifier.padding(primary: Dp = 0.dp, secondary: Dp = 0.dp): Modifier =
        if (isLandscape) {
            padding(horizontal = primary, vertical = secondary)
        } else {
            padding(vertical = primary, horizontal = secondary)
        }

    fun <T> adaptive(landscape: T, portrait: T) = if (isLandscape) landscape else portrait
}

private class AdaptiveLayoutScopeImpl(private val delegate: BoxWithConstraintsScope) :
    BoxWithConstraintsScope by delegate, AdaptiveLayoutScope

@Composable
fun AdaptiveLayoutScope.AdaptiveDivider(modifier: Modifier = Modifier) {
    if (isLandscape) {
        VerticalDivider(modifier)
    } else {
        HorizontalDivider(modifier)
    }
}

@Composable
fun AdaptiveLayout(
    arrangement: Arrangement.HorizontalOrVertical,
    modifier: Modifier = Modifier,
    content: @Composable AdaptiveLayoutScope.() -> Unit
) = BoxWithConstraints {
        val scope = remember(this) { AdaptiveLayoutScopeImpl(this) }
        if (scope.isLandscape) {
            Row(horizontalArrangement = arrangement, modifier = modifier) { with(scope) { content() } }
        } else {
            Column(verticalArrangement = arrangement, modifier = modifier) { with(scope) { content() } }
        }
    }