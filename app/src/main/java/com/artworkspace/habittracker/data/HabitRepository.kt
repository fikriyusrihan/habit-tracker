package com.artworkspace.habittracker.data

import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.HabitRecord
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import com.artworkspace.habittracker.data.room.HabitDao
import com.artworkspace.habittracker.utils.todayTimestamp
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {
    /**
     * Get all uncompleted habit by timestamp
     */
    fun getUncompletedHabit(timestamp: Long): Flow<List<HabitRecord>> =
        habitDao.getUncompletedHabitRecord(timestamp)

    /**
     * Get all completed habit by timestamp
     */
    fun getCompletedHabit(timestamp: Long): Flow<List<HabitRecord>> =
        habitDao.getCompletedHabitRecord(timestamp)

    /**
     * Get all started habit by timestamp.
     * By default, timestamp using today timestamp
     */
    fun getAllStartedHabit(timestamp: Long = todayTimestamp): Flow<List<Habit>> =
        habitDao.getAllStartedHabit(timestamp)

    /**
     * Set habit record completion status by isChecked value.
     * By default, timestamp using today timestamp
     */
    suspend fun setHabitRecordCheck(
        habit: Habit,
        isChecked: Boolean,
        timestamp: Long = todayTimestamp
    ) {
        val oldRecord = habitDao.getHabitRecord(habit.id!!, timestamp)
        if (oldRecord != null) {
            val newRecord = Record(
                id = oldRecord.id,
                habitId = oldRecord.habitId,
                isChecked = isChecked,
                timestamp = oldRecord.timestamp
            )
            habitDao.updateRecord(newRecord)
        }
    }

    /**
     * Get record size by timestamp.
     * By default, timestamp using today timestamp
     */
    suspend fun getRecordSizeByTimestamp(timestamp: Long = todayTimestamp): Int {
        return habitDao.getCountRecordByTimestamp(timestamp)
    }

    /**
     * Get record from correspond habit id.
     * By default, timestamp using today timestamp
     */
    suspend fun getHabitRecord(habit: Habit, timestamp: Long = todayTimestamp): Record? =
        habitDao.getHabitRecord(habit.id!!, timestamp)

    /**
     * Determine a Habit is repeated today or not based on timestamp
     *
     * @param habit Habit to check
     * @param timestamp Timestamp to check
     */
    suspend fun isHabitRepeatToday(habit: Habit, timestamp: Long): Boolean {
        val sdf = SimpleDateFormat("EEE", Locale.ENGLISH)
        val date = Date(timestamp)
        val txtDay = sdf.format(date).uppercase()

        val weeklyTarget = habitDao.getHabitWeeklyTarget(habit.id!!)
        val map = mapOf(
            "MON" to weeklyTarget.component3(),
            "TUE" to weeklyTarget.component4(),
            "WED" to weeklyTarget.component5(),
            "THU" to weeklyTarget.component6(),
            "FRI" to weeklyTarget.component7(),
            "SAT" to weeklyTarget.component8(),
            "SUN" to weeklyTarget.component9()
        )
        return map[txtDay] ?: false
    }

    /**
     * Insert weekly target data of a habit to the database
     */
    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget) =
        habitDao.insertWeeklyTarget(weeklyTarget)

    /**
     * Insert new habit to the database
     */
    suspend fun insertHabit(habits: Habit) = habitDao.insertHabit(habits)

    /**
     * Insert new daily record to the database
     */
    suspend fun insertDailyRecord(record: Record) = habitDao.insertRecord(record)
}