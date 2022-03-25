package com.artworkspace.habittracker.data

import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.room.HabitDao
import javax.inject.Inject

class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {
    suspend fun insertHabit(vararg habits: Habit) = habitDao.insertHabit(*habits)
}