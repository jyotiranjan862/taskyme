package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val pendingTasks = db.taskDao().getAllTasksSync().filter { !it.isCompleted }

                    for (task in pendingTasks) {
                        if (task.reminderMinutesBefore >= 0 && task.deadlineTimestamp != null) {
                            NotificationHelper.scheduleTaskReminder(context, task)
                        }
                    }

                    // Check if persistent notification should be restored
                    val nextTask = db.taskDao().getNextUpcomingDeadlineSync()
                    if (nextTask != null && nextTask.task.isPersistentNotificationEnabled) {
                        val cat = db.categoryDao().getCategoryById(nextTask.task.categoryId)
                        NotificationHelper.showPersistentDeadlineNotification(context, nextTask.task, cat)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
