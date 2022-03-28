package com.artworkspace.habittracker.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.WeeklyTarget

@Dao
interface HabitDao {

    @Query(
        "SELECT habit.id, name, icon, description, start_at, created_at " +
                "FROM habit " +
                "INNER JOIN record ON habit.id = record.habit_id " +
                "WHERE record.is_checked = 0 AND Record.timestamp = :timestamp"
    )
    fun getUncompletedHabitRecord(timestamp: Long): LiveData<List<Habit>>

    @Query("SELECT COUNT(id) FROM record WHERE timestamp = :timestamp")
    suspend fun getCountRecordByTimestamp(timestamp: Long): Int

    @Query("SELECT * FROM habit WHERE start_at >= :timestamp")
    suspend fun getAllStartedHabit(timestamp: Long): List<Habit>

    @Query("SELECT * FROM record WHERE habit_id = :id")
    suspend fun getRecordByHabitId(id: Long): Record?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(vararg record: Record)
}