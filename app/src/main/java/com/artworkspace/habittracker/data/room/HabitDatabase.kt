package com.artworkspace.habittracker.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.artworkspace.habittracker.data.entity.*

@Database(
    entities = [Habit::class, Record::class, WeeklyTarget::class, HabitRecord::class, ReminderTime::class],
    version = 1,
    exportSchema = false
)

abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}