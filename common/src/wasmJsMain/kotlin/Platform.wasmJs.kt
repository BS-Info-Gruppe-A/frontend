package eu.bsinfo

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

private val mobileRegex = "Android|iPhone".toRegex()

actual val Dispatchers.IO: CoroutineDispatcher get() = Default

actual val isMobile: Boolean get() = window.navigator.userAgent.matches(mobileRegex)
