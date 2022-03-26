package com.artworkspace.habittracker.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.WeeklyTarget

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget)
}