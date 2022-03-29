package com.artworkspace.habittracker.data

import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import com.artworkspace.habittracker.data.room.HabitDao
import com.artworkspace.habittracker.utils.todayTimestamp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {

    fun getUncompletedHabit(timestamp: Long): Flow<List<Habit>> =
        habitDao.getUncompletedHabitRecord(timestamp)

    suspend fun getRecordSizeByTimestamp(timestamp: Long = todayTimestamp): Int {
        return habitDao.getCountRecordByTimestamp(timestamp)
    }

    suspend fun getAllStartedHabit(timestamp: Long = todayTimestamp): List<Habit> {
        return habitDao.getAllStartedHabit(timestamp)
    }

    suspend fun setHabitAsDone(habit: Habit, timestamp: Long = todayTimestamp) {
        val oldRecord = habitDao.getHabitRecord(habit.id!!, timestamp)
        val newRecord = Record(
            id = oldRecord.id,
            habitId = oldRecord.habitId,
            isChecked = true,
            timestamp = oldRecord.timestamp
        )

        habitDao.updateRecord(newRecord)
    }

    suspend fun getHabitRecord(id: Long, timestamp: Long = todayTimestamp): Record {
        return habitDao.getHabitRecord(id, timestamp)
    }

    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget) =
        habitDao.insertWeeklyTarget(weeklyTarget)

    suspend fun insertHabit(habits: Habit) = habitDao.insertHabit(habits)

    suspend fun insertDailyRecord(record: Record) = habitDao.insertRecord(record)
}