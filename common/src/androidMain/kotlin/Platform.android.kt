package eu.bsinfo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Suppress("EXTENSION_SHADOWED_BY_MEMBER") // It doesn't on commonMain
actual val Dispatchers.IO: CoroutineDispatcher get() = IO // This returns the member Dispatchers.IO

actual val isMobile: Boolean = true
