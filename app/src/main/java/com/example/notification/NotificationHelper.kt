package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_DEADLINES_ID = "taskflow_deadlines"
    const val CHANNEL_PERSISTENT_ID = "taskflow_persistent"

    const val ACTION_COMPLETE_TASK = "com.example.ACTION_COMPLETE_TASK"
    const val ACTION_ALARM_TRIGGER = "com.example.ACTION_ALARM_TRIGGER"
    const val ACTION_DISMISS_NOTIFICATION = "com.example.ACTION_DISMISS_NOTIFICATION"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"

    const val NOTIFICATION_ID_PERSISTENT = 9001
    const val NOTIFICATION_ID_ALARM_BASE = 10000

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val deadlineChannel = NotificationChannel(
                CHANNEL_DEADLINES_ID,
                "Task Deadline Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts and alarms for approaching task deadlines"
                enableVibration(true)
            }

            val persistentChannel = NotificationChannel(
                CHANNEL_PERSISTENT_ID,
                "Persistent Active Deadline",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing status of your next approaching deadline"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(deadlineChannel)
            notificationManager.createNotificationChannel(persistentChannel)
        }
    }

    fun showPersistentDeadlineNotification(
        context: Context,
        task: TaskEntity,
        category: CategoryEntity?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val timeString = formatRelativeDeadline(task.deadlineTimestamp)
        val categoryLabel = category?.name ?: "Task"

        // Open app intent
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark complete intent
        val completeIntent = Intent(context, TaskReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PERSISTENT_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(task.title)
            .setContentText("$timeString • $categoryLabel")
            .setSubText("Next Deadline")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                R.mipmap.ic_launcher,
                "Mark Done ✓",
                completePendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID_PERSISTENT, notification)
    }

    fun cancelPersistentNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_PERSISTENT)
    }

    fun showDeadlineAlarmNotification(context: Context, taskId: Long, title: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completeIntent = Intent(context, TaskReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DEADLINES_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Deadline Alert!")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                R.mipmap.ic_launcher,
                "Mark Done ✓",
                completePendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID_ALARM_BASE + taskId.toInt(), notification)
    }

    fun scheduleTaskReminder(context: Context, task: TaskEntity) {
        val deadline = task.deadlineTimestamp ?: return
        if (task.reminderMinutesBefore < 0 || task.isCompleted) return

        val triggerTime = deadline - (task.reminderMinutesBefore * 60 * 1000L)
        if (triggerTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, TaskReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun formatRelativeDeadline(deadline: Long?): String {
        if (deadline == null) return "No deadline"
        val diff = deadline - System.currentTimeMillis()
        if (diff < 0) {
            val overdueMins = (-diff / (60 * 1000)).toInt()
            return if (overdueMins < 60) "Overdue by ${overdueMins}m" else "Overdue by ${overdueMins / 60}h"
        }
        val mins = (diff / (60 * 1000)).toInt()
        val hours = mins / 60
        val days = hours / 24
        return when {
            mins < 60 -> "Due in ${mins}m"
            hours < 24 -> "Due in ${hours}h ${mins % 60}m"
            days == 1 -> "Due tomorrow"
            else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(deadline))
        }
    }
}
