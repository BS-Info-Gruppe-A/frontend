package eu.bsinfo.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.bsinfo.file_dialog.FileDialogCancelException
import eu.bsinfo.file_dialog.Filter
import eu.bsinfo.isMobile
import eu.bsinfo.util.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
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
    noinline importItem: suspend (T) -> Unit,
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
                    BigTastyBacon(importItem, items, serializer, viewModel)

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
fun <T> BigTastyBacon(
    importItem: suspend (T) -> Unit,
    items: List<T>,
    serializer: KSerializer<T>,
    model: EntityViewModel
) {
    val scope = rememberCoroutineScope()
    val exporterState = rememberExporterState()

    var expanded by remember { mutableStateOf(false) }
    var importPath by remember { mutableStateOf<FileHandle?>(null) }

    IconButton({ expanded = true }) {
        Icon(
            Icons.Default.Menu,
            contentDescription = "More options",
            modifier = Modifier.size(50.dp)
        )
    }

    DropdownMenu(expanded, { expanded = false }) {
        DropdownMenuItem({ Text("Import") }, {
            scope.launch {
                try {
                    importPath = chooseFile(*formats.keys.map {
                        Filter(it, it)
                    }.toTypedArray())
                } catch (_: FileDialogCancelException) {
                    expanded = false
                }
            }
        }, leadingIcon = { Icon(Icons.AutoMirrored.Default.Login, "import") })
        ExporterDropdownEntry(exporterState, { expanded = false })
    }

    Importer(importPath, importItem, serializer, { expanded = false; importPath = null }, model)
    Exporter(exporterState, items, serializer, { expanded = false })
}

@Composable
fun <T> DataProcessor(
    path: FileHandle?, serializer: KSerializer<T>, onClose: () -> Unit,
    done: @Composable () -> Unit = {},
    running: @Composable () -> Unit = {},
    doneDescription: @Composable () -> Unit = {},
    additionalButtons: (@Composable () -> Unit)? = null,
    processor: suspend (StringFormat, FileHandle, KSerializer<T>) -> Unit,
    onDone: () -> Unit = {}
) {
    var processing by remember(path) { mutableStateOf(true) }
    var invalidFileExtension by remember(path) { mutableStateOf(false) }

    if (path != null) {
        LaunchedEffect(path) {
            val extension = path.extension
            val format = formats[extension]
            if (format == null) {
                invalidFileExtension = true
            } else {
                processor(format, path, serializer)
            }
            onDone()
            processing = false
        }

        AlertDialog(
            { if (!processing) onClose() },
            icon = { Icon(Icons.AutoMirrored.Default.Logout, "Export") },
            title = {
                if (invalidFileExtension) {
                    Text("Ungültige Dateiendung!")
                } else if (!processing) {
                    done()
                }
            },
            dismissButton = { if(!invalidFileExtension) additionalButtons?.invoke() },
            confirmButton = {
                if (invalidFileExtension || !processing) {
                    Button(onClose) {
                        Icon(Icons.Default.Check, "Ok")
                        Text("OK")
                    }
                }
            },
            text = {
                if (invalidFileExtension) {
                    Text("Bitte verwende json, csv oder xml", textAlign = TextAlign.Center)
                } else if (processing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                        running()
                    }
                } else {
                    doneDescription()
                }
            }
        )
    }
}
