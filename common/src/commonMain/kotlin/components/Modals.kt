package eu.bsinfo.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import eu.bsinfo.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DeleteDialog(
    present: Boolean,
    onDismiss: () -> Unit,
    onSubmit: suspend CoroutineScope.() -> Unit,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onError) {
        Dialog(
            present, onDismiss, onSubmit,
            MaterialTheme.colorScheme.error,
            title = title,
            text = text,
            icon = {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                )
            },
            confirmButtonContent = {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = "Eintrag löschen",
                )
                Text("Eintrag löschen")
            },
            enabled = enabled,
        )
    }
}

@Composable
private fun Dialog(
    present: Boolean,
    onDismiss: () -> Unit,
    onSubmit: suspend CoroutineScope.() -> Unit,
    backgroundColor: Color,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    confirmButtonContent: @Composable RowScope.() -> Unit,
    confirmButtonColors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true,
) {
    if (present) {
        var loading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { onDismiss() },
            containerColor = backgroundColor,
            confirmButton = {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            loading = true
                            scope.launch(Dispatchers.IO) {
                                onSubmit()
                                loading = false
                                onDismiss()
                            }
                        },
                        colors = confirmButtonColors,
                        content = confirmButtonContent,
                        enabled = enabled
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { onDismiss() },
                    enabled = !loading
                ) {
                    Text("Abbrechen")
                }
            },
            icon = icon,
            title = title,
            text = text
        )
    }
}
