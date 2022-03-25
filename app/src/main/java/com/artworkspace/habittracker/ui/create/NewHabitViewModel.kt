package com.artworkspace.habittracker.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NewHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private var _startAtTimestamp = MutableLiveData(Calendar.getInstance().timeInMillis)
    val startAtTimestamp: LiveData<Long> = _startAtTimestamp

    fun insertHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.insertHabit(habit)
        }
    }

    fun setStartAtTimestamp(timeInMillis: Long) {
        _startAtTimestamp.value = timeInMillis
    }
}