package eu.bsinfo.util

import kotlinx.browser.window
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

private val options = DateFormatterOptions()

actual fun formatLocalDate(date: LocalDate): String {
    val date = date.atStartOfDayIn(TimeZone.currentSystemDefault())
    val jsDate = Date(date.toEpochMilliseconds().toDouble())
    val formatter = DateTimeFormat(window.navigator.language.toJsString(), options)

    return formatter.format(jsDate).toString()
}
