package com.artworkspace.habittracker.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.HabitRecord
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.WeeklyTarget

@Database(
    entities = [Habit::class, Record::class, WeeklyTarget::class, HabitRecord::class],
    version = 1,
    exportSchema = false
)

abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}