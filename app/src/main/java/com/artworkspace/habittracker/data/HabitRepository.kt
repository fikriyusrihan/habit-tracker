package com.artworkspace.habittracker.data

import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import com.artworkspace.habittracker.data.room.HabitDao
import javax.inject.Inject

class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {
    suspend fun insertHabit(habits: Habit) = habitDao.insertHabit(habits)

    suspend fun insertWeeklyTarget(weeklyTarget: WeeklyTarget) =
        habitDao.insertWeeklyTarget(weeklyTarget)
}