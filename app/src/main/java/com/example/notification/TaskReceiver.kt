package com.example.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.AppDatabase
import com.example.widget.TaskFlowWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)

        when (action) {
            NotificationHelper.ACTION_COMPLETE_TASK -> {
                if (taskId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val now = System.currentTimeMillis()
                            db.taskDao().updateTaskCompletion(taskId, isCompleted = true, completedAt = now)

                            // Cancel alarm notification if showing
                            val notificationManager =
                                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(NotificationHelper.NOTIFICATION_ID_ALARM_BASE + taskId.toInt())

                            // Cancel any pending alarms
                            NotificationHelper.cancelTaskReminder(context, taskId)

                            // Check and update persistent notification with next upcoming task
                            val nextTask = db.taskDao().getNextUpcomingDeadlineSync()
                            if (nextTask != null && nextTask.task.isPersistentNotificationEnabled) {
                                val cat = db.categoryDao().getCategoryById(nextTask.task.categoryId)
                                NotificationHelper.showPersistentDeadlineNotification(context, nextTask.task, cat)
                            } else {
                                NotificationHelper.cancelPersistentNotification(context)
                            }

                            // Update widget
                            TaskFlowWidgetProvider.updateAllWidgets(context)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            NotificationHelper.ACTION_ALARM_TRIGGER -> {
                val title = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE) ?: "Task deadline approaching"
                if (taskId != -1L) {
                    NotificationHelper.showDeadlineAlarmNotification(context, taskId, title)
                }
            }
        }
    }
}
