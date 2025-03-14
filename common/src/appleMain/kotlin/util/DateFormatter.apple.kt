package eu.bsinfo.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.systemLocale

actual fun formatLocalDate(context: PlatformContext, date: LocalDate): String {
    val format = NSDateFormatter().apply {
        setTimeStyle(NSDateFormatterShortStyle)
        setLocale(NSLocale.systemLocale())
    }
    return format.stringFromDate(date.atStartOfDayIn(TimeZone.currentSystemDefault()).toNSDate())
}
