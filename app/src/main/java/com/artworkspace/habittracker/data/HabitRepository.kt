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
     * Get all uncompleted habit by timestamp in Flow
     *
     * @param timestamp Specified timestamp
     */
    fun getUncompletedHabit(timestamp: Long): Flow<List<HabitRecord>> =
        habitDao.getUncompletedHabitRecordsInFlow(timestamp)

    /**
     * Get all completed habit by timestamp in Flow
     *
     * @param timestamp Specified timestamp
     */
    fun getCompletedHabit(timestamp: Long): Flow<List<HabitRecord>> =
        habitDao.getCompletedHabitRecordsInFlow(timestamp)

    /**
     * Get all started habit by timestamp in Flow.
     * By default, timestamp using today timestamp
     *
     * @param timestamp Specified timestamp, by default using today timestamp
     */
    fun getAllStartedHabit(timestamp: Long = todayTimestamp): Flow<List<Habit>> =
        habitDao.getAllStartedHabitInFlow(timestamp)

    /**
     * Get all reminder time that related with a habit in LiveData
     *
     * @param habit Related habit
     */
    fun getReminderTimeLiveData(habit: Habit): Flow<ReminderTime> =
        habitDao.getHabitReminderTimeInFlow(habit.id!!)

    /**
     * Get all records in a Flow
     */
    fun getAllRecord(): Flow<List<Record>> = habitDao.getAllRecordsInFlow()

    /**
     * Get all records related to a habit in a LiveData
     *
     * @param habit Related habit
     */
    fun getAllHabitRecords(habit: Habit): LiveData<List<Record>> =
        habitDao.getAllRecordByHabitIdInLiveData(habit.id!!)


    /**
     * Get a habit by id, that returned a Flow
     *
     * @param id Habit's id
     * @return Flow
     */
    fun getHabitByIdInFlow(id: Long): Flow<Habit> = habitDao.getHabitByIdInFlow(id)

    /**
     * Get WeeklyTarget that related with a habit in Flow
     *
     * @param id Related Habit's id
     * @return Flow
     */
    fun getHabitWeeklyTargetInFlow(id: Long): Flow<WeeklyTarget> =
        habitDao.getHabitWeeklyTargetInFlow(id)

    /**
     * Get a habit by id
     *
     * @param id Habit's id
     * @return Habit?
     */
    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    /**
     * Get ReminderData that related with a habit
     *
     * @param habit Related habit
     * @return ReminderTime
     */
    suspend fun getReminderTime(habit: Habit): ReminderTime =
        habitDao.getHabitReminderTime(habit.id!!)


    /**
     * Get `record` size by `timestamp`.
     * By default, timestamp using today timestamp
     *
     * @param timestamp Specified timestamp, by default using today timestamp
     */
    suspend fun getRecordSizeByTimestamp(timestamp: Long = todayTimestamp): Int {
        return habitDao.getCountRecordByTimestamp(timestamp)
    }

    /**
     * Get `record` from related habit id.
     * By default, timestamp using today timestamp
     *
     * @param habit Related habit
     * @param timestamp Timestamp to collect, by default using today timestamp
     */
    suspend fun getHabitRecord(habit: Habit, timestamp: Long = todayTimestamp): Record? =
        habitDao.getHabitRecordAtTimestamp(habit.id!!, timestamp)

    /**
     * Get weekly target that related with a habit
     *
     * @param habit Habit that related with
     * @return WeeklyTarget
     */
    suspend fun getWeeklyTargetByHabit(habit: Habit): WeeklyTarget =
        habitDao.getHabitWeeklyTarget(habit.id!!)

    /**
     * Count total completed of a habit
     * @param habit Habit record to count
     * @return Num of record
     */
    suspend fun getCountCompletedHabit(habit: Habit): Int =
        habitDao.getCountCompletedHabit(habit.id!!)

    /**
     * Count total habit's record
     *
     * @param habit Habit record to count
     * @return Num of record
     */
    suspend fun getCountAllRecordOfHabit(habit: Habit): Int {
        var countAllHabitRecord = habitDao.getCountAllHabitRecord(habit.id!!)

        if (isHabitRepeatToday(habit, tomorrowTimestamp)) {
            countAllHabitRecord--
        }

        return countAllHabitRecord
    }

    /**
     * Set habit record completion status by `isChecked` value.
     * By default, `timestamp` using today timestamp
     *
     * @param habit Related habit
     * @param isChecked Habit completion status
     * @param timestamp Specified timestamp, by default using today timestamp
     */
    suspend fun setHabitRecordCheck(
        habit: Habit,
        isChecked: Boolean,
        timestamp: Long = todayTimestamp
    ) {
        val oldRecord = habitDao.getHabitRecordAtTimestamp(habit.id!!, timestamp)
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
     * Updating existing habit data
     *
     * @param habit `Habit` data to update
     * @param weeklyTarget `WeeklyTarget` to update
     * @param reminderTime `ReminderTime` to update
     */
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
     * Insert new habit to the database
     *
     * @param habit Habit to insert
     */
    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)

    /**
     * Insert new daily record to the database
     *
     * @param record Record to insert
     */
    suspend fun insertDailyRecord(record: Record) = habitDao.insertRecord(record)

    /**
     * Insert reminder time data of a habit to the database
     *
     * @param reminderTime ReminderTime to insert
     */
    suspend fun insertReminderTime(reminderTime: ReminderTime) =
        habitDao.insertReminderTime(reminderTime)

    /**
     * Insert `WeeklyTarget` data of a habit to the database
     *
     * @param weeklyTarget WeeklyTarget to insert
     */
    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget) =
        habitDao.insertWeeklyTarget(weeklyTarget)

    /**
     * Delete habit and its related entities (`Records`, `WeeklyTarget`, and `ReminderTime`)
     *
     * @param habit Habit to delete
     */
    suspend fun deleteHabit(habit: Habit) {
        val id = habit.id!!

        habitDao.deleteHabitById(id)
        habitDao.deleteAllRecordsByHabitId(id)
        habitDao.deleteWeeklyTargetByHabitId(id)
        habitDao.deleteReminderTimeByHabitId(id)
    }

    /**
     * Delete habit record before certain time
     *
     * @param habit Habit
     * @param timestamp Specify time
     */
    suspend fun deleteHabitRecordBefore(habit: Habit, timestamp: Long) =
        habitDao.deleteRecordBeforeTimestamp(habit.id!!, timestamp)

    /**
     * Determine a Habit is repeated today or not based on `timestamp`
     *
     * @param habit Habit to check
     * @param timestamp Timestamp to check
     * @return Boolean
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
}