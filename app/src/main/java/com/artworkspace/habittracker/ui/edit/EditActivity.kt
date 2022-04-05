package com.artworkspace.habittracker.ui.edit

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
import com.artworkspace.habittracker.databinding.ActivityEditBinding
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
class EditActivity : AppCompatActivity(), IconDialog.Callback {

    private lateinit var binding: ActivityEditBinding
    private lateinit var habit: Habit
    private lateinit var notificationReceiver: NotificationReceiver

    private var checkedDaysState: BooleanArray = booleanArrayOf()
    private var reminderState: ReminderTime? = null
    private var startAtState: Long? = null
    private var iconState: Int? = null

    private val viewModel: EditViewModel by viewModels()
    private val days =
        arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habit = intent.getParcelableExtra(EXTRA_EDIT)!!
        notificationReceiver = NotificationReceiver()

        viewModel.apply {
            val owner = this@EditActivity
            habitTitleState.observe(owner) { name ->
                setHabitNameText(name)
            }
            habitDescriptionState.observe(owner) { description ->
                setHabitDescriptionText(description)
            }
            habitIconState.observe(owner) { icon ->
                setHabitIconDrawable(icon)
                iconState = icon
            }
            habitStartState.observe(owner) { timestamp ->
                setHabitStartAtText(timestamp)
                startAtState = timestamp
            }
            habitReminderState.observe(owner) { reminderTime ->
                setHabitReminderText(reminderTime)
                reminderState = reminderTime
            }
            habitCheckedDaysState.observe(owner) { checkedDays ->
                setHabitRepeatText(checkedDays)
                checkedDaysState = checkedDays
            }

            initializeData(habit)
        }

        binding.apply {
            btnExit.setOnClickListener { finish() }
            btnSave.setOnClickListener { saveHabit() }
            btnSelectIcon.setOnClickListener { showHabitIconSelector() }
            repeatStatus.setOnClickListener { showSelectCheckedDaysDialog(this@EditActivity) }
            reminderStatus.setOnClickListener { showTimePicker() }
            startFromStatus.setOnClickListener { showStartDatePicker() }
        }
    }

    override val iconDialogIconPack: IconPack?
        get() = (application as BaseApplication).iconPack

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        val icon = icons.first()
        viewModel.setIconState(icon.id)
    }

    /**
     * Set and parse icon information to its related imageview
     *
     * @param id Icon id
     */
    private fun setHabitIconDrawable(id: Int?) {
        if (id != null) {
            binding.btnSelectIcon.setImageDrawable(iconDialogIconPack?.getIcon(id)?.drawable)
        }
    }

    /**
     * Set and parse data of habit's name to its related textview
     *
     * @param name Information to parse, Habit's name
     */
    private fun setHabitNameText(name: String) {
        binding.etHabitTitle.setText(name)
    }

    /**
     * Set and parse data of habit's description to its related textview
     *
     * @param description Information to parse
     */
    private fun setHabitDescriptionText(description: String) {
        binding.etHabitDescription.setText(description)
    }

    /**
     * Set and parse data of checked days to its related textview
     *
     * @param checkedDays Information to parse
     */
    private fun setHabitRepeatText(checkedDays: BooleanArray) {
        var string = ""
        var isEveryday = true
        checkedDays.forEachIndexed { index, b ->
            if (b) {
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
     * Show dialog icon picker for the habit
     */
    private fun showHabitIconSelector() {
        val iconDialog = supportFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
            ?: IconDialog.newInstance(IconDialogSettings())

        iconDialog.show(supportFragmentManager, ICON_DIALOG_TAG)
    }

    /**
     * Set the start from text view
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
     * Set default reminder for every new habit
     */
    private fun setHabitReminderText(time: ReminderTime) {
        val sdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
        }

        binding.tvReminderStatus.text =
            getString(R.string.remind_me_at, sdf.format(calendar.time))
    }

    /**
     * Show daily repeat dialog
     */
    private fun showSelectCheckedDaysDialog(ctx: Context) {
        MaterialAlertDialogBuilder(ctx)
            .setTitle(getString(R.string.repeat_this_habit_every))
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                viewModel.setCheckedDaysState(checkedDaysState)
            }
            .setMultiChoiceItems(days, checkedDaysState) { _, which, checked ->
                checkedDaysState[which] = checked
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show time picker for daily reminder
     */
    private fun showTimePicker() {
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(reminderState?.hour ?: 9)
            .setMinute(reminderState?.minute ?: 0)
            .setTitleText(getString(R.string.daily_reminder))
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    ReminderTime(
                        id = reminderState?.id,
                        habitId = habit.id!!,
                        hour = this.hour,
                        minute = this.minute
                    ).also {
                        viewModel.setReminderTimeState(it)
                    }
                }
                show(supportFragmentManager, TIME_PICKER_TAG)
            }
    }

    /**
     * Show and set the date picker for start date field
     */
    private fun showStartDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(startAtState)
            .build()

        datePicker.show(supportFragmentManager, DATE_PICKER_TAG)
        datePicker.addOnPositiveButtonClickListener {
            viewModel.setStartAtState(it)
        }
    }

    /**
     * Save habit that contain new information
     */
    private fun saveHabit() {
        val name = binding.etHabitTitle.text.toString()
        val description = binding.etHabitDescription.text.toString()
        val icon = iconState
        val startAt = startAtState!!

        if (name.isBlank()) {
            binding.etHabitTitle.error = getString(R.string.please_fill_this_field)
        } else {
            val newHabit = Habit(
                id = habit.id,
                name = name,
                icon = icon,
                description = description,
                startAt = startAt,
                createdAt = habit.createdAt
            )

            lifecycleScope.launch {
                viewModel.saveHabit(
                    habit = newHabit,
                    checkedDays = checkedDaysState,
                    reminderTime = reminderState!!
                )

                viewModel.deleteHabitRecordBeforeTimestamp(newHabit, startAt)

                val calendar = Calendar.getInstance()
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, reminderState?.hour ?: 9)
                    set(Calendar.MINUTE, reminderState?.minute ?: 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                notificationReceiver.cancelAlarm(
                    this@EditActivity, newHabit
                )

                notificationReceiver.setReminderNotification(
                    this@EditActivity,
                    newHabit,
                    calendar.timeInMillis
                )
            }

            Toast.makeText(this, getString(R.string.habit_information_updated), Toast.LENGTH_SHORT)
                .show()
            finish()
        }
    }

    companion object {
        const val EXTRA_EDIT = "extra_edit"
        private const val ICON_DIALOG_TAG = "IconDialog"
        private const val TIME_PICKER_TAG = "TimePicker"
        private const val DATE_PICKER_TAG = "DatePicker"
    }
}