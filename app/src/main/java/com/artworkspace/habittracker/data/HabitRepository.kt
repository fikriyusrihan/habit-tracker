package com.artworkspace.habittracker.data

import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import com.artworkspace.habittracker.data.room.HabitDao
import java.util.*
import javax.inject.Inject

class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {

    private fun getTodayTimeInMillis(): Long {
        val todayCalendar = Calendar.getInstance()
        todayCalendar.apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return todayCalendar.timeInMillis
    }

    fun getUncompletedHabit(timestamp: Long) = habitDao.getUncompletedHabitRecord(timestamp)

    suspend fun getRecordSizeByTimestamp(timestamp: Long? = null): Int {
        return if (timestamp == null) {
            val today = getTodayTimeInMillis()
            habitDao.getCountRecordByTimestamp(today)
        } else {
            habitDao.getCountRecordByTimestamp(timestamp)
        }
    }

    suspend fun getAllStartedHabit(timestamp: Long? = null): List<Habit> {
        return if (timestamp == null) {
            val today = getTodayTimeInMillis()
            habitDao.getAllStartedHabit(today)
        } else {
            habitDao.getAllStartedHabit(timestamp)
        }
    }

    suspend fun getRecordByHabitId(id: Long) = habitDao.getRecordByHabitId(id)

    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget) =
        habitDao.insertWeeklyTarget(weeklyTarget)

    suspend fun insertHabit(habits: Habit) = habitDao.insertHabit(habits)

    suspend fun insertDailyRecord(vararg record: Record) = habitDao.insertRecord(*record)
}