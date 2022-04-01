package com.artworkspace.habittracker.utils

import java.util.*

val todayTimestamp: Long
    get() {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }

val tomorrowTimestamp: Long
    get() {
        val calendar = Calendar.getInstance()
        calendar.apply {
            timeInMillis = todayTimestamp
            add(Calendar.DATE, 1)
        }

        return calendar.timeInMillis
    }