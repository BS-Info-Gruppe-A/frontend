package eu.bsinfo.util

import kotlinx.browser.window
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

private val options = DateFormatterOptions()

actual fun formatLocalDate(context: PlatformContext, date: LocalDate): String {
    val dateTime = date.atStartOfDayIn(TimeZone.currentSystemDefault())
    val jsDate = Date(dateTime.toEpochMilliseconds().toDouble())
    val formatter = DateTimeFormat(window.navigator.language.toJsString(), options)

    return formatter.format(jsDate).toString()
}
