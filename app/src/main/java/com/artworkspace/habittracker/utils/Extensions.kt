package com.artworkspace.habittracker.utils

import android.content.Context
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(year, month)

val YearMonth.next: YearMonth
    get() = this.plusMonths(1)

internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

internal fun TextView.setTextColorRes(@ColorRes color: Int) =
    setTextColor(context.getColorCompat(color))

fun daysOfWeekFromLocale(): Array<DayOfWeek> {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    var daysOfWeek = DayOfWeek.values()
    // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
    // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
    if (firstDayOfWeek != DayOfWeek.MONDAY) {
        val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
        val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
        daysOfWeek = rhs + lhs
    }
    return daysOfWeek
}
