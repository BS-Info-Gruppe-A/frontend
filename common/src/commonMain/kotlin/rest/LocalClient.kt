package eu.bsinfo.rest

import androidx.compose.runtime.staticCompositionLocalOf
import eu.bsinfo.data.Client

val LocalClient = staticCompositionLocalOf { Client() }
