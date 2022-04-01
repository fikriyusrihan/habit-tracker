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
    fun getUncompletedHabitRecord(timestamp: Long): Flow<List<HabitRecord>>

    @Query(
        "SELECT habit.id, name, icon, description, is_checked, start_at, created_at " +
                "FROM record " +
                "INNER JOIN habit ON habit.id = record.habit_id " +
                "WHERE record.is_checked = 1 AND Record.timestamp = :timestamp"
    )
    fun getCompletedHabitRecord(timestamp: Long): Flow<List<HabitRecord>>

    @Query("SELECT * FROM record WHERE habit_id = :habitId ORDER BY timestamp DESC")
    fun getAllRecord(habitId: Long): LiveData<List<Record>>

    @Query("SELECT COUNT(id) FROM record WHERE timestamp = :timestamp")
    suspend fun getCountRecordByTimestamp(timestamp: Long): Int

    @Query("SELECT COUNT(id) FROM record WHERE habit_id = :habitId")
    suspend fun getCountAllHabitRecord(habitId: Long): Int

    @Query("SELECT COUNT(id) FROM record WHERE habit_id = :habitId AND is_checked = 1 AND timestamp <= :timestamp")
    suspend fun getCountCompletedHabit(habitId: Long, timestamp: Long = todayTimestamp): Int

    @Query("SELECT * FROM habit WHERE start_at <= :timestamp")
    fun getAllStartedHabit(timestamp: Long): Flow<List<Habit>>

    @Query("SELECT * FROM record WHERE habit_id = :id AND timestamp = :timestamp")
    suspend fun getHabitRecord(id: Long, timestamp: Long): Record?

    @Query("SELECT * FROM weeklytarget WHERE habit_id = :id")
    suspend fun getHabitWeeklyTarget(id: Long): WeeklyTarget

    @Query("SELECT * FROM remindertime WHERE habit_id = :id")
    fun getHabitReminderTime(id: Long): LiveData<ReminderTime>

    @Query("DELETE FROM record WHERE habit_id = :habitId")
    suspend fun deleteAllRecordsByHabitId(habitId: Long)

    @Query("DELETE FROM weeklytarget WHERE habit_id = :habitId")
    suspend fun deleteWeeklyTargetByHabitId(habitId: Long)

    @Query("DELETE FROM remindertime WHERE habit_id = :habitId")
    suspend fun deleteReminderTimeByHabitId(habitId: Long)

    @Query("DELETE FROM habit WHERE id = :habitId")
    suspend fun deleteHabit(habitId: Long)

    @Update
    suspend fun updateRecord(record: Record)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReminderTime(reminderTime: ReminderTime)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: Record)
}