package com.artworkspace.habittracker.data.room

import androidx.room.*
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.HabitRecord
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.WeeklyTarget
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

    @Query("SELECT COUNT(id) FROM record WHERE timestamp = :timestamp")
    suspend fun getCountRecordByTimestamp(timestamp: Long): Int

    @Query("SELECT * FROM habit WHERE start_at <= :timestamp")
    fun getAllStartedHabit(timestamp: Long): Flow<List<Habit>>

    @Query("SELECT * FROM record WHERE habit_id = :id AND timestamp = :timestamp")
    suspend fun getHabitRecord(id: Long, timestamp: Long): Record?

    @Query("SELECT * FROM weeklytarget WHERE habit_id = :id")
    suspend fun getHabitWeeklyTarget(id: Long): WeeklyTarget

    @Update
    suspend fun updateRecord(record: Record)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: Record)
}