package com.artworkspace.habittracker.ui.progress

import androidx.lifecycle.ViewModel
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(private val habitRepository: HabitRepository) :
    ViewModel() {

    fun getAllRecord(): Flow<List<Record>> = habitRepository.getAllRecord()

    fun getAllStartedHabit(): Flow<List<Habit>> = habitRepository.getAllStartedHabit()
}