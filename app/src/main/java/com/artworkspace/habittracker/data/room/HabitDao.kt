package com.artworkspace.habittracker.data.room

import androidx.room.Dao
import androidx.room.Insert
import com.artworkspace.habittracker.data.entity.Habit

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(vararg habit: Habit)
}