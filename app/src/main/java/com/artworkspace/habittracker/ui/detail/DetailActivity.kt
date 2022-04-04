package com.artworkspace.habittracker.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.ReminderTime
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import com.artworkspace.habittracker.databinding.ActivityDetailBinding
import com.artworkspace.habittracker.databinding.CalendarDayLayoutBinding
import com.artworkspace.habittracker.notification.NotificationReceiver
import com.artworkspace.habittracker.ui.edit.EditActivity
import com.artworkspace.habittracker.ui.edit.EditActivity.Companion.EXTRA_EDIT
import com.artworkspace.habittracker.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var habit: Habit
    private lateinit var notificationReceiver: NotificationReceiver

    private val detailViewModel: DetailViewModel by viewModels()

    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habit = intent.getParcelableExtra(EXTRA_DETAIL)!!
        notificationReceiver = NotificationReceiver()

        // Initialize data to fetch
        detailViewModel.apply {
            getWeeklyTarget(habit)
            getTotalCompleted(habit)
            getTotalAllRecord(habit)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }


        detailViewModel.apply {
            val observer = this@DetailActivity
            weeklyTarget.observe(observer) { weeklyTarget ->
                parseWeeklyTarget(weeklyTarget)
            }

            totalCompleted.observe(observer) { totalCompleted ->
                parseTotalCompleted(totalCompleted)
            }

            completionRate.observe(observer) { completionRate ->
                parseCompletionRate(completionRate)
            }

            getReminderTime(habit).observe(observer) { reminderTime ->
                parseReminderTime(reminderTime)
            }

            getAllHabitRecord(habit).observe(observer) { records ->
                countCurrentStreak(records)
                parseCalendarHistory(records)
            }
        }

        binding.toolbarLayout.title = habit.name

        if (habit.description.isNotBlank()) {
            binding.tvDescriptionStatus.visibility = View.VISIBLE
            binding.tvHabitDescription.apply {
                visibility = View.VISIBLE
                text = habit.description
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_edit -> {
            Intent(this, EditActivity::class.java).also { intent ->
                intent.putExtra(EXTRA_EDIT, habit)
                startActivity(intent)
            }
            true
        }
        R.id.menu_delete -> {
            deleteHabit(habit)
            notificationReceiver.cancelAlarm(this, habit)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun parseWeeklyTarget(weeklyTarget: WeeklyTarget) {
        var isEveryday = true
        var string = ""
        val daysInWeek = arrayOf('M', 'T', 'W', 'T', 'F', 'S', 'S')
        val checkedDays = booleanArrayOf(
            weeklyTarget.mon,
            weeklyTarget.tue,
            weeklyTarget.wed,
            weeklyTarget.thu,
            weeklyTarget.fri,
            weeklyTarget.sat,
            weeklyTarget.sun
        )

        checkedDays.forEachIndexed { index, checked ->
            if (checked) string += if (string.isBlank()) daysInWeek[index] else ", ${daysInWeek[index]}"
            else isEveryday = false
        }

        if (!isEveryday)
            binding.tvRepeatAt.text = string
    }

    private fun parseReminderTime(reminderTime: ReminderTime?) {
        if (reminderTime != null) {
            val sdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminderTime.hour)
                set(Calendar.MINUTE, reminderTime.minute)
            }

            binding.tvReminderAt.text = sdf.format(calendar.time)
        }
    }

    private fun parseTotalCompleted(totalCompleted: Int) {
        binding.tvTotalCompleted.text =
            getString(R.string.total_completed_placeholder, totalCompleted)
        animateViewVisibility(true, binding.cardTotalCompleted)
    }

    private fun parseCompletionRate(completionRate: Int) {
        val string = "$completionRate %"
        binding.tvCompletionRate.text = string
        animateViewVisibility(true, binding.cardCompletionRate)
    }

    private fun parseCalendarHistory(records: List<Record>) {

        val checkedRecords = records.filter { it.isChecked }
        checkedRecords.forEach { record ->
            val calendar = Calendar.getInstance().also { it.timeInMillis = record.timestamp }
            val localDate =
                LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()).toLocalDate()
            selectedDates.add(localDate)
        }

        val firstDayInRecord = Calendar.getInstance()
        val lastDayInRecord = Calendar.getInstance()

        try {
            firstDayInRecord.timeInMillis = records.last().timestamp
            lastDayInRecord.timeInMillis = records.first().timestamp
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

        animateViewVisibility(true, binding.calendarContainer)
    }

    private fun deleteHabit(habit: Habit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.are_you_sure))
            .setMessage(getString(R.string.your_habit_will_be_deleted))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                detailViewModel.deleteRecord(habit)
                finish()
            }
            .show()
    }

    private fun countCurrentStreak(habitRecords: List<Record>) {
        var streakCounter = 0

        for (i: Int in habitRecords.indices) {
            if (!habitRecords[i].isChecked) {
                if (!(habitRecords[i].timestamp == todayTimestamp || habitRecords[i].timestamp == tomorrowTimestamp)) break
            } else {
                streakCounter++
            }
        }

        binding.tvCounterStreak.text = getString(R.string.streak_counter, streakCounter)
        animateViewVisibility(true, binding.cardStreak)
    }

    companion object {
        const val EXTRA_DETAIL = "extra_detail"
    }
}