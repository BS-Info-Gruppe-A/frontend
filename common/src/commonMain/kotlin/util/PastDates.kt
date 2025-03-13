package eu.bsinfo.util

import androidx.compose.material3.SelectableDates
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object PastDates: SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year <= Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    }
}
