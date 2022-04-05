package com.artworkspace.habittracker.ui.progress

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.databinding.CalendarDayLayoutBinding
import com.artworkspace.habittracker.databinding.FragmentProgressBinding
import com.artworkspace.habittracker.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val progressViewModel by viewModels<ProgressViewModel>()

    private val selectedDates = mutableSetOf<LocalDate>()
    private val partialSelectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            launch {
                progressViewModel.getAllRecord().collect { records ->
                    countCurrentStreak(records)
                    countTotalHabitCompleted(records)
                    countDailyAverage(records)
                    countCompletionRate(records)
                    parseCalendarHistory(records)
                }
            }

            launch {
                progressViewModel.getAllStartedHabit().collect { habits ->
                    countStartedHabit(habits)
                }
            }
        }

        binding.apply {
            btnShowInformation.setOnClickListener {
                showProgressInformation(requireContext())
            }

            cardStreak.setOnLongClickListener {
                Toast.makeText(requireContext(), getString(R.string.keep_it_up), Toast.LENGTH_SHORT)
                    .show()
                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Show information dialog of this fragment
     *
     * @param context Context
     */
    private fun showProgressInformation(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.progress_information_title))
            .setMessage(getString(R.string.progress_information_message))
            .setPositiveButton(getString(R.string.progress_information_ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Count current streak from all habits
     *
     * @param records All records
     */
    private fun countCurrentStreak(records: List<Record>) {
        var streakCounter = 0
        var lastTimestamp = 0L

        for (i in records.indices) {
            if (!(records[i].isChecked)) {
                if (!(records[i].timestamp == todayTimestamp || records[i].timestamp == tomorrowTimestamp)) break
            } else {
                if (lastTimestamp != records[i].timestamp) {
                    streakCounter++
                    lastTimestamp = records[i].timestamp
                } else {
                    continue
                }
            }
        }

        binding.tvCounterStreak.text = getString(R.string.streak_counter, streakCounter)
        binding.cardStreak.animateVisibility(true)
    }

    /**
     * Count total habit that already completed and parse the data to related views
     *
     * @param records All habit's records
     */
    private fun countTotalHabitCompleted(records: List<Record>) {
        var counter = 0
        records.forEach { record ->
            if (record.isChecked) counter++
        }

        binding.tvTotalCompleted.text = getString(R.string.total_completed_placeholder, counter)
        binding.cardTotalHabitCompleted.animateVisibility(true)
    }

    /**
     * Count number of started habits and parse the data to related views
     *
     * @param habits List of habits
     */
    private fun countStartedHabit(habits: List<Habit>) {
        binding.tvStartedHabit.text = getString(R.string.started_habit_placeholder, habits.size)
        binding.cardHabitInProgress.animateVisibility(true)
    }

    /**
     * Count daily average of completed habits and parse the information to related views
     *
     * @param records List of records
     */
    private fun countDailyAverage(records: List<Record>) {
        var daysCounter = 0
        var completedHabitCounter = 0
        var lastTimestamp = 0L

        val sortedRecords = records.sortedByDescending {
            it.timestamp
        }

        for (sortedRecord in sortedRecords) {
            if (sortedRecord.timestamp != lastTimestamp) {
                if (sortedRecord.timestamp == tomorrowTimestamp) {
                    if (sortedRecord.isChecked) {
                        daysCounter++
                        lastTimestamp = sortedRecord.timestamp
                    }
                } else {
                    daysCounter++
                    lastTimestamp = sortedRecord.timestamp
                }
            }

            if (sortedRecord.isChecked) completedHabitCounter++
        }

        val avg = completedHabitCounter.toDouble() / daysCounter
        binding.tvAvgDaily.text = getString(R.string.double_placeholder, avg)
        binding.cardDailyAverage.animateVisibility(true)
    }

    /**
     * Count completion rate of all habits and parse the data to related views
     *
     * @param records List of records
     */
    private fun countCompletionRate(records: List<Record>) {
        val completedCounter =
            records.filter { it.isChecked && it.timestamp <= todayTimestamp }.size
        val recordSize = records.filter { it.timestamp <= todayTimestamp }.size
        val completionRate = ((completedCounter.toDouble() / recordSize) * 100).toInt()
        val string = "$completionRate %"

        binding.tvCompletionRate.text = string
        binding.cardCompletionRate.animateVisibility(true)
    }

    /**
     * Parse information to the calendar views
     *
     * @param records List of records
     */
    private fun parseCalendarHistory(records: List<Record>) {

        val checkedRecords = records.filter { it.isChecked }
        checkedRecords.forEach { record ->
            val calendar = Calendar.getInstance().also { it.timeInMillis = record.timestamp }
            val localDate =
                LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()).toLocalDate()
            selectedDates.add(localDate)
        }

        val uncheckedRecords = records.filter { !it.isChecked }
        uncheckedRecords.forEach { record ->
            val calendar = Calendar.getInstance().also { it.timeInMillis = record.timestamp }
            val localDate =
                LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()).toLocalDate()

            if (selectedDates.contains(localDate)) {
                selectedDates.remove(localDate)
                partialSelectedDates.add(localDate)
            }
        }

        val firstDayInRecord =
            Calendar.getInstance().also { it.timeInMillis = records.last().timestamp }
        val lastDayInRecord =
            Calendar.getInstance().also { it.timeInMillis = records.first().timestamp }

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        val startMonth =
            YearMonth.of(
                firstDayInRecord.get(Calendar.YEAR),
                firstDayInRecord.get(Calendar.MONTH) + 1
            )
        val endMonth =
            YearMonth.of(
                lastDayInRecord.get(Calendar.YEAR),
                lastDayInRecord.get(Calendar.MONTH) + 1
            )

        binding.legendLayout.root.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH)
                    .uppercase()
            }
        }
        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
        }

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    when {
                        selectedDates.contains(day.date) -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.selected_date_background)
                        }
                        partialSelectedDates.contains(day.date) -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.selected_date_border)
                        }
                        today == day.date -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.icon_background_circle)
                        }
                        else -> {
                            textView.background = null
                        }
                    }
                } else {
                    textView.setTextColor(Color.TRANSPARENT)
                    textView.background = null
                }
            }
        }

        binding.calendarView.monthScrollListener = {
            if (binding.calendarView.maxRowCount == 6) {
                binding.calendarMonthYear.text = monthTitleFormatter.format(it.yearMonth)
            } else {
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth || firstDate.year == lastDate.year) {
                    binding.calendarMonthYear.text = monthTitleFormatter.format(firstDate)
                } else {
                    val string = "${monthTitleFormatter.format(firstDate)} - ${
                        monthTitleFormatter.format(lastDate)
                    }"
                    binding.calendarMonthYear.text = string

                }
            }
        }

        binding.calendarContainer.animateVisibility(true)
    }
}