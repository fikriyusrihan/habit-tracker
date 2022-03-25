package com.artworkspace.habittracker.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    fun insertHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.insertHabit(habit)
        }
    }
}