package eu.bsinfo.util

import kotlinx.browser.window

private val format = NumberFormat(window.navigator.language.toJsString(), NumberFormatterOptions())
private val baseFormat = NumberFormat(window.navigator.language.toJsString())
private val regex = Regex("\\d")
private val thousandSeparator = baseFormat.format(11111.0).toString().replace(regex, "")
private val decimalSeparator = baseFormat.format(1.1).toString().replace(regex, "")

actual fun formatDecimal(value: Double): String = format.format(value).toString()
actual fun parseDecimal(value: String): Double? = parseLocaleNumber(value.toJsString())

// JS is garbage: https://stackoverflow.com/a/29273131
private fun parseLocaleNumber(input: JsString): Double? {

    return input
        .toString()
        .replace(thousandSeparator, "")
        .replace(decimalSeparator, ".")
        .toDoubleOrNull()
}

