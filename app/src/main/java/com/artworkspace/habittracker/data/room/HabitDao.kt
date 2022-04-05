package com.artworkspace.habittracker.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.artworkspace.habittracker.data.entity.*
import com.artworkspace.habittracker.utils.todayTimestamp
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query(
        "SELECT habit.id, name, icon, description, is_checked, start_at, created_at " +
                "FROM record " +
                "INNER JOIN habit ON habit.id = record.habit_id " +
                "WHERE record.is_checked = 0 AND Record.timestamp = :timestamp"
    )
    fun getUncompletedHabitRecordsInFlow(timestamp: Long): Flow<List<HabitRecord>>

    @Query(
        "SELECT habit.id, name, icon, description, is_checked, start_at, created_at " +
                "FROM record " +
                "INNER JOIN habit ON habit.id = record.habit_id " +
                "WHERE record.is_checked = 1 AND Record.timestamp = :timestamp"
    )
    fun getCompletedHabitRecordsInFlow(timestamp: Long): Flow<List<HabitRecord>>

    @Query("SELECT * FROM habit WHERE start_at <= :timestamp")
    fun getAllStartedHabitInFlow(timestamp: Long): Flow<List<Habit>>

    @Query("SELECT * FROM record ORDER BY timestamp DESC")
    fun getAllRecordsInFlow(): Flow<List<Record>>

    @Query("SELECT * FROM habit WHERE id = :habitId")
    fun getHabitByIdInFlow(habitId: Long): Flow<Habit>

    @Query("SELECT * FROM weekly_target WHERE habit_id = :habitId")
    fun getHabitWeeklyTargetInFlow(habitId: Long): Flow<WeeklyTarget>

    @Query("SELECT * FROM reminder_time WHERE habit_id = :id")
    fun getHabitReminderTimeInFlow(id: Long): Flow<ReminderTime>

    @Query("SELECT * FROM record WHERE habit_id = :habitId ORDER BY timestamp DESC")
    fun getAllRecordByHabitIdInLiveData(habitId: Long): LiveData<List<Record>>

    @Query("SELECT COUNT(id) FROM record WHERE timestamp = :timestamp")
    suspend fun getCountRecordByTimestamp(timestamp: Long): Int

    @Query("SELECT COUNT(id) FROM record WHERE habit_id = :habitId")
    suspend fun getCountAllHabitRecord(habitId: Long): Int

    @Query("SELECT COUNT(id) FROM record WHERE habit_id = :habitId AND is_checked = 1 AND timestamp <= :timestamp")
    suspend fun getCountCompletedHabit(habitId: Long, timestamp: Long = todayTimestamp): Int

    @Query("SELECT * FROM habit WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Query("SELECT * FROM record WHERE habit_id = :id AND timestamp = :timestamp")
    suspend fun getHabitRecordAtTimestamp(id: Long, timestamp: Long): Record?

    @Query("SELECT * FROM weekly_target WHERE habit_id = :id")
    suspend fun getHabitWeeklyTarget(id: Long): WeeklyTarget

    @Query("SELECT * FROM reminder_time WHERE habit_id = :id")
    suspend fun getHabitReminderTime(id: Long): ReminderTime

    @Query("DELETE FROM record WHERE habit_id = :habitId")
    suspend fun deleteAllRecordsByHabitId(habitId: Long)

    @Query("DELETE FROM weekly_target WHERE habit_id = :habitId")
    suspend fun deleteWeeklyTargetByHabitId(habitId: Long)

    @Query("DELETE FROM reminder_time WHERE habit_id = :habitId")
    suspend fun deleteReminderTimeByHabitId(habitId: Long)

    @Query("DELETE FROM habit WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    @Query("DELETE FROM record WHERE habit_id = :habitId AND timestamp < :timestamp")
    suspend fun deleteRecordBeforeTimestamp(habitId: Long, timestamp: Long)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Update
    suspend fun updateRecord(record: Record)

    @Update
    suspend fun updateWeeklyTarget(weeklyTarget: WeeklyTarget)

    @Update
    suspend fun updateReminderTime(reminderTime: ReminderTime)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReminderTime(reminderTime: ReminderTime)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: Record)
}