package com.artworkspace.habittracker.ui.journal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.HabitRecord
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.utils.todayTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(private val habitRepository: HabitRepository) :
    ViewModel() {

    private val _calendarHorizontalData = MutableLiveData<ArrayList<Long>>()
    val calendarHorizontalData: LiveData<ArrayList<Long>> = _calendarHorizontalData

    init {
        getHorizontalCalendarData()
    }

    fun getUncompletedHabit(timestamp: Long = todayTimestamp): Flow<List<HabitRecord>> =
        habitRepository.getUncompletedHabit(timestamp)

    fun getCompletedHabit(timestamp: Long = todayTimestamp): Flow<List<HabitRecord>> =
        habitRepository.getCompletedHabit(timestamp)

    fun recordInit(timestamp: Long) {
        viewModelScope.launch {
            val count = habitRepository.getRecordSizeByTimestamp(timestamp)

            habitRepository.getAllStartedHabit(timestamp).collect { habits ->
                if (habits.size != count) {
                    habits.forEach { habit ->
                        val oldRecord = habitRepository.getHabitRecord(habit, timestamp)
                        if (oldRecord == null) {
                            val newRecord = Record(
                                id = null,
                                habitId = habit.id!!,
                                isChecked = false,
                                timestamp = timestamp
                            )
                            habitRepository.insertDailyRecord(newRecord)
                        }
                    }
                }
            }
        }
    }

    fun setHabitRecordCheck(habit: Habit, isChecked: Boolean, timestamp: Long = todayTimestamp) {
        viewModelScope.launch {
            habitRepository.setHabitRecordCheck(habit, isChecked, timestamp)
        }
    }

    private fun getHorizontalCalendarData() {
        val calendarData = arrayListOf<Long>()
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, 1)
        }

        repeat(30) {
            calendarData.add(calendar.timeInMillis)
            calendar.add(Calendar.DATE, -1)
        }

        calendarData.sort()

        _calendarHorizontalData.value = calendarData
    }
}