package eu.bsinfo.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import eu.bsinfo.isMobile
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

interface EntityViewState {
    val query: String
}

interface EntityViewModel {
    val uiState: StateFlow<EntityViewState>
    suspend fun refresh()

    fun setSearchQuery(text: String)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T> EntityContainer(
    viewModel: EntityViewModel,
    items: List<T>,
    serializer: KSerializer<T> = serializer(),
    noinline addButtonIcon: @Composable () -> Unit = {},
    noinline addButtonText: @Composable () -> Unit = {},
    noinline searchPlaceholder: @Composable () -> Unit = {},
    noinline content: @Composable () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusRequester = LocalFocusManager.current

    PullToRefreshBox(isRefreshing, {
        scope.launch {
            isRefreshing = true
            viewModel.refresh()
            isRefreshing = false
        }
    }, modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                val interactionSource = remember { MutableInteractionSource() }
                val isExpanded by interactionSource.collectIsHoveredAsState()

                ExtendedFloatingActionButton(
                    onClick = {},
                    expanded = isExpanded,
                    text = addButtonText,
                    icon = addButtonIcon,
                    modifier = Modifier.hoverable(interactionSource)
                )
            },
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp)
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BigTastyBacon(items, serializer)

                    DockedSearchBar(
                        inputField = {
                            var refreshing by remember { mutableStateOf(false) }

                            SearchBarDefaults.InputField(
                                state.query, viewModel::setSearchQuery,
                                expanded = false,
                                placeholder = searchPlaceholder,
                                onExpandedChange = {},
                                onSearch = { },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                trailingIcon = {
                                    if (!isMobile) { // On mobile you can use pull to refresh
                                        if (refreshing) {
                                            CircularProgressIndicator(Modifier.size(ButtonDefaults.IconSize))
                                        } else {
                                            IconButton(onClick = {
                                                scope.launch {
                                                    refreshing = true
                                                    viewModel.refresh()
                                                    refreshing = false
                                                }
                                            }) {
                                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        false, {}, modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 15.dp)
                            .fillMaxWidth()
                            .onKeyEvent {
                                if (it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                                    focusRequester.clearFocus()
                                    true
                                } else {
                                    false
                                }
                            }
                    ) {}
                }
                content()
            }
        }
    }
}

@Composable
expect fun <T> HamburgerItems(items: List<T>, serializer: KSerializer<T>)

@Composable
fun <T> BigTastyBacon(items: List<T>, serializer: KSerializer<T>) {
    var expanded by remember { mutableStateOf(false) }

    IconButton({ expanded = true }) {
        Icon(
            Icons.Default.Menu,
            contentDescription = "More options",
            modifier = Modifier.size(50.dp)
        )
    }

    DropdownMenu(expanded, { expanded = false }) {
        HamburgerItems(items, serializer)
    }
}
