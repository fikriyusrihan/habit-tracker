package com.artworkspace.habittracker.ui.create

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.artworkspace.habittracker.BaseApplication
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.ReminderTime
import com.artworkspace.habittracker.databinding.ActivityNewHabitBinding
import com.artworkspace.habittracker.notification.NotificationReceiver
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateHabitActivity : AppCompatActivity(), IconDialog.Callback {

    private lateinit var binding: ActivityNewHabitBinding
    private lateinit var notificationReceiver: NotificationReceiver

    private var startAtState: Long? = null
    private var reminderState: ReminderTime? = null
    private var checkedDaysState: BooleanArray? = null
    private var iconState: Icon? = null

    private val viewModel: CreateHabitViewModel by viewModels()
    private val days =
        arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationReceiver = NotificationReceiver()

        if (savedInstanceState != null) {
            binding.tvRepeatStatus.text = savedInstanceState.getString(REPEAT_STATUS)
        }

        viewModel.apply {
            val observer = this@CreateHabitActivity

            habitStartAtState.observe(observer) { timeInMillis ->
                setHabitStartAtText(timeInMillis)
                startAtState = timeInMillis
            }
            habitReminderTimeState.observe(observer) { time ->
                setHabitReminderTimeText(time)
                reminderState = time
            }
            habitCheckedDaysState.observe(observer) { checkedDays ->
                setHabitWeeklyTargetText(checkedDays)
                checkedDaysState = checkedDays
            }
            habitIconState.observe(observer) { icon ->
                iconState = icon
                binding.btnSelectIcon.setImageDrawable(icon?.drawable)
            }
        }

        binding.apply {
            btnExit.setOnClickListener { finish() }
            btnSave.setOnClickListener { saveNewHabit() }
            btnSelectIcon.setOnClickListener { showHabitIconDialog() }
            cardWeeklyTargetStatus.setOnClickListener { showHabitWeeklyTargetDialog(this@CreateHabitActivity) }
            cardReminderTimeStatus.setOnClickListener { showTimePickerDialog() }
            cardStartAtStatus.setOnClickListener { showHabitStartDateDialog() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(REPEAT_STATUS, binding.tvRepeatStatus.text.toString())
        super.onSaveInstanceState(outState)
    }

    override val iconDialogIconPack: IconPack?
        get() = (application as BaseApplication).iconPack

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        val icon = icons.first()
        binding.btnSelectIcon.setImageDrawable(icon.drawable)
        viewModel.setHabitIconState(icon)
    }

    /**
     * Show icon picker dialog for the habit
     */
    private fun showHabitIconDialog() {
        val iconDialog = supportFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
            ?: IconDialog.newInstance(IconDialogSettings())

        iconDialog.show(supportFragmentManager, ICON_DIALOG_TAG)
    }

    /**
     * Show time picker for daily reminder
     */
    private fun showTimePickerDialog() {
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(reminderState?.hour ?: 9)
            .setMinute(reminderState?.minute ?: 0)
            .setTitleText(getString(R.string.daily_reminder))
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    val reminderTime = ReminderTime(hour = this.hour, minute = this.minute)
                    viewModel.setHabitReminderTimeState(reminderTime)
                }
                show(supportFragmentManager, TIME_PICKER_TAG)
            }
    }

    /**
     * Show and set the date picker for start date field
     */
    private fun showHabitStartDateDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(startAtState)
            .build()

        datePicker.show(supportFragmentManager, DATE_PICKER_TAG)
        datePicker.addOnPositiveButtonClickListener {
            viewModel.setHabitStartAtState(it)
        }
    }

    /**
     * Show daily repeat dialog
     *
     * @param context context to pass
     */
    private fun showHabitWeeklyTargetDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.repeat_this_habit_every))
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                viewModel.setHabitCheckedDaysState(checkedDaysState)
            }
            .setMultiChoiceItems(days, checkedDaysState) { _, which, checked ->
                checkedDaysState?.set(which, checked)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Set habit starting time on related textview
     *
     * @param timeInMillis timestamp to parse
     */
    private fun setHabitStartAtText(timeInMillis: Long) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
        }
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }

        val date = Date(timeInMillis)
        val sdf = SimpleDateFormat.getDateInstance()

        if (timeInMillis in todayStart.timeInMillis..todayEnd.timeInMillis) {
            binding.tvStartFromStatus.text = getString(R.string.start_from_today)
        } else {
            binding.tvStartFromStatus.text = getString(R.string.start_from, sdf.format(date))
        }
    }

    /**
     * Set habit reminder status on related textview
     *
     * @param time `ReminderTime` to parse
     */
    private fun setHabitReminderTimeText(time: ReminderTime) {
        val sdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
        }

        binding.tvReminderStatus.text =
            getString(R.string.remind_me_at, sdf.format(calendar.time))
    }

    /**
     * Set habit weekly target status on related textview
     *
     * @param checkedDays checked days state
     */
    private fun setHabitWeeklyTargetText(checkedDays: BooleanArray) {
        var string = ""
        var isEveryday = true

        checkedDays.forEachIndexed { index, value ->
            if (value) {
                string += if (string.isNotBlank()) {
                    ", ${days[index]}"
                } else {
                    days[index]
                }
            } else {
                isEveryday = false
            }
        }

        if (isEveryday) binding.tvRepeatStatus.text = getString(R.string.repeat_everyday)
        else binding.tvRepeatStatus.text = getString(R.string.repeat_every, string)
    }

    /**
     * Save and store the new habit to the database
     */
    private fun saveNewHabit() {
        val name = binding.etHabitTitle.text.toString()
        val description = binding.etHabitDescription.text.toString()

        val icon = iconState?.id
        val startAt = startAtState!!

        if (name.isBlank()) {
            binding.etHabitTitle.error = getString(R.string.please_fill_this_field)
        } else {
            val habit = Habit(
                id = null,
                name = name,
                icon = icon,
                description = description,
                startAt = startAt,
                createdAt = Calendar.getInstance().timeInMillis
            )

            lifecycleScope.launch {
                val habitId = viewModel.saveNewHabit(
                    habit = habit,
                    checkedDays = checkedDaysState,
                    reminderTime = reminderState!!
                )

                val calendar = Calendar.getInstance()
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, reminderState?.hour ?: 9)
                    set(Calendar.MINUTE, reminderState?.minute ?: 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val storedHabit = Habit(
                    id = habitId,
                    name = name,
                    icon = icon,
                    description = description,
                    startAt = startAt,
                    createdAt = habit.createdAt
                )

                notificationReceiver.setReminderNotification(
                    this@CreateHabitActivity,
                    storedHabit,
                    calendar.timeInMillis
                )
            }

            Toast.makeText(this, getString(R.string.new_habit_created), Toast.LENGTH_SHORT)
                .show()
            finish()
        }
    }

    companion object {
        private const val REPEAT_STATUS = "RepeatStatus"
        private const val ICON_DIALOG_TAG = "IconDialog"
        private const val TIME_PICKER_TAG = "TimePicker"
        private const val DATE_PICKER_TAG = "DatePicker"
    }
}