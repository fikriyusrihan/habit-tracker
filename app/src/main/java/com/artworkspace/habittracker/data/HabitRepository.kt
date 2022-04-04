package com.artworkspace.habittracker.data

import androidx.lifecycle.LiveData
import com.artworkspace.habittracker.data.entity.*
import com.artworkspace.habittracker.data.room.HabitDao
import com.artworkspace.habittracker.utils.todayTimestamp
import com.artworkspace.habittracker.utils.tomorrowTimestamp
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {

    /**
     * Get a habit by id
     */
    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

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
     * Get all reminder time for a habit
     */
    fun getReminderTimeLiveData(habit: Habit): LiveData<ReminderTime> =
        habitDao.getHabitReminderTimeLiveData(habit.id!!)

    suspend fun getReminderTime(habit: Habit): ReminderTime = habitDao.getHabitReminder(habit.id!!)

    suspend fun updateHabitData(
        habit: Habit,
        weeklyTarget: WeeklyTarget,
        reminderTime: ReminderTime
    ) {
        habitDao.updateHabit(habit)
        habitDao.updateReminderTime(reminderTime)
        habitDao.updateWeeklyTarget(weeklyTarget)
    }

    /**
     * Get all records
     */
    fun getAllRecord(): Flow<List<Record>> = habitDao.getAllRecord()

    /**
     * Get all records of a habit
     */
    fun getAllHabitRecords(habit: Habit): LiveData<List<Record>> =
        habitDao.getAllRecordByHabitId(habit.id!!)

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
     * Get weekly target by habit id
     */
    suspend fun getWeeklyTargetByHabit(habit: Habit): WeeklyTarget =
        habitDao.getHabitWeeklyTarget(habit.id!!)

    /**
     * Count total completed of a habit
     */
    suspend fun getCountCompletedHabit(habit: Habit): Int =
        habitDao.getCountCompletedHabit(habit.id!!)

    /**
     * Count total habit's record
     */
    suspend fun getCountAllHabitRecord(habit: Habit): Int {
        var countAllHabitRecord = habitDao.getCountAllHabitRecord(habit.id!!)

        if (isHabitRepeatToday(habit, tomorrowTimestamp)) {
            countAllHabitRecord--
        }

        return countAllHabitRecord
    }

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
     * Insert reminder time data of a habit to the database
     */
    suspend fun insertReminderTime(reminderTime: ReminderTime) =
        habitDao.insertReminderTime(reminderTime)

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

    /**
     * Delete habit and its correspond entities (records, weekly target, and reminder time)
     */
    suspend fun deleteHabit(habit: Habit) {
        val id = habit.id!!

        habitDao.deleteHabit(id)
        habitDao.deleteAllRecordsByHabitId(id)
        habitDao.deleteWeeklyTargetByHabitId(id)
        habitDao.deleteReminderTimeByHabitId(id)
    }
}