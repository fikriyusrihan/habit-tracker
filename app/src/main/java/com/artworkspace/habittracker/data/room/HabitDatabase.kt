package com.artworkspace.habittracker.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.artworkspace.habittracker.data.entity.*
import com.artworkspace.habittracker.data.entity.Unit

@Database(
    entities = [Habit::class, Record::class, Unit::class, DailyTarget::class, WeeklyTarget::class],
    version = 1
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}