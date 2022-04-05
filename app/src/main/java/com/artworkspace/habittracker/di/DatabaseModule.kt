package com.artworkspace.habittracker.di

import android.content.Context
import androidx.room.Room
import com.artworkspace.habittracker.data.room.HabitDao
import com.artworkspace.habittracker.data.room.HabitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun provideHabitDao(habitDatabase: HabitDatabase): HabitDao = habitDatabase.habitDao()

    @Provides
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HabitDatabase::class.java,
            "habit_database"
        ).build()
    }
}