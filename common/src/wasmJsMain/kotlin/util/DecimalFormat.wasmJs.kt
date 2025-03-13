package eu.bsinfo.util

import kotlinx.browser.window

private val format = NumberFormat(window.navigator.language.toJsString(), NumberFormatterOptions())

actual fun formatDecimal(value: Double): String = format.format(value).toString()
