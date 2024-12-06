package eu.bsinfo

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "KotlinRedundantDiagnosticSuppress") // It doesn't on commonMain
expect val Dispatchers.IO: CoroutineDispatcher

@Composable
expect fun isSystemInDarkMode(): Boolean
