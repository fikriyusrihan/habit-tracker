package com.artworkspace.habittracker.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.ui.MainActivity
import com.artworkspace.habittracker.utils.todayTimestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitRepository: HabitRepository

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, ID_REPEATING)
        val title = intent.getStringExtra(EXTRA_TITLE)

        runBlocking {
            val habit = habitRepository.getHabitById(id.toLong())
            if (habit != null) {
                val isRepeatedToday = habitRepository.isHabitRepeatToday(habit, todayTimestamp)
                if (isRepeatedToday) showReminderNotification(context, id, title)
            }
        }
    }

    /**
     * Setting up reminder notification by creating alarm manager with daily interval
     */
    fun setReminderNotification(context: Context, habit: Habit, timeInMillis: Long) {
        val intent = Intent(context, NotificationReceiver::class.java).also {
            it.putExtra(EXTRA_TITLE, habit.name)
            it.putExtra(EXTRA_ID, habit.id?.toInt() ?: ID_REPEATING)
        }
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                habit.id?.toInt() ?: ID_REPEATING,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC,
            timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    /**
     * Send notification to the users
     */
    private fun showReminderNotification(context: Context, id: Int, title: String?) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_application_logo)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(
                context.getString(
                    R.string.reminder_notification_message,
                    title ?: ""
                )
            )
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_NAME

            builder.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notificationManager.notify(id, notification)
    }

    /**
     * Cancel the alarm manager
     */
    fun cancelAlarm(context: Context, habit: Habit) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val requestCode = habit.id?.toInt() ?: ID_REPEATING
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        pendingIntent.cancel()
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val CHANNEL_ID = "Reminder"
        private const val CHANNEL_NAME = "Habit Reminder Notification"
        private const val ID_REPEATING = 101

        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_ID = "extra_id"
    }
}