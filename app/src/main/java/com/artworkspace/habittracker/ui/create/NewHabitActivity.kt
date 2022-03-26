package com.artworkspace.habittracker.ui.create

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.artworkspace.habittracker.BaseApplication
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.databinding.ActivityNewHabitBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class NewHabitActivity : AppCompatActivity(), View.OnClickListener, IconDialog.Callback {

    private lateinit var binding: ActivityNewHabitBinding

    private var startAtTimestamp: Long? = null
    private var iconId: Int? = null

    private val viewModel: NewHabitViewModel by viewModels()
    private val days =
        arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val checkedDays = booleanArrayOf(true, true, true, true, true, true, true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.startAtTimestamp.observe(this) { timeInMillis ->
            startAtTimestamp = timeInMillis
            setDefaultStartAt(timeInMillis)
        }

        binding.apply {
            btnExit.setOnClickListener(this@NewHabitActivity)
            btnSave.setOnClickListener(this@NewHabitActivity)
            btnSelectIcon.setOnClickListener(this@NewHabitActivity)
            repeatStatus.setOnClickListener(this@NewHabitActivity)
            startFromStatus.setOnClickListener(this@NewHabitActivity)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnExit.id -> {
                finish()
            }

            binding.btnSave.id -> {
                saveNewHabit()
            }

            binding.btnSelectIcon.id -> {
                selectHabitIcon()
            }

            binding.repeatStatus.id -> {
                showRepeatDialog(this)
            }

            binding.startFromStatus.id -> {
                setStartDate()
            }
        }
    }

    override val iconDialogIconPack: IconPack?
        get() = (application as BaseApplication).iconPack

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        val icon = icons.first()

        binding.btnSelectIcon.setImageDrawable(icon.drawable)
        iconId = icon.id
    }

    private fun selectHabitIcon() {
        val iconDialog = supportFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
            ?: IconDialog.newInstance(IconDialogSettings())

        iconDialog.show(supportFragmentManager, ICON_DIALOG_TAG)
    }

    private fun setDefaultStartAt(timeInMillis: Long) {
        val date = Date(timeInMillis)
        val sdf = SimpleDateFormat.getDateInstance()

        binding.tvStartFromStatus.text = getString(R.string.start_from, sdf.format(date))
    }

    private fun setStartDate() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(startAtTimestamp)
            .build()

        datePicker.show(supportFragmentManager, TAG)
        datePicker.addOnPositiveButtonClickListener {
            viewModel.setStartAtTimestamp(it)
        }
    }

    private fun showRepeatDialog(ctx: Context) {
        MaterialAlertDialogBuilder(ctx)
            .setTitle(getString(R.string.repeat))
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                var string = ""
                var isEveryday = true
                checkedDays.forEachIndexed { index, b ->
                    if (b) {
                        string += if (index != 0) {
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
            .setMultiChoiceItems(days, checkedDays) { _, which, checked ->
                checkedDays[which] = checked
            }
            .setCancelable(false)
            .show()
    }

    private fun saveNewHabit() {
        val name = binding.etHabitTitle.text.toString()
        val icon = iconId
        val startAt = startAtTimestamp!!

        if (name.isBlank()) {
            binding.etHabitTitle.error = getString(R.string.please_fill_this_field)
            Toast.makeText(this, getString(R.string.please_fill_this_field), Toast.LENGTH_SHORT)
                .show()
        } else {
            viewModel.saveNewHabit(
                habit = Habit(
                    id = null,
                    name = name,
                    icon = icon,
                    description = "",
                    startAt = startAt,
                    createdAt = Calendar.getInstance().timeInMillis
                ),
                weeklyTargetArray = checkedDays,
                dailyTarget = 1
            )

            Toast.makeText(this, getString(R.string.new_habit_created), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        private const val TAG = "NewHabitActivity"
        private const val ICON_DIALOG_TAG = "IconDialog"
    }


}