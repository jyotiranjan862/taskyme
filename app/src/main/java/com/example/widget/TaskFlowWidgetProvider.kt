package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskFlowWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_COMPLETE_TASK) {
            val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
            if (taskId != -1L) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val now = System.currentTimeMillis()
                        db.taskDao().updateTaskCompletion(taskId, isCompleted = true, completedAt = now)

                        // Cancel notifications and alarms
                        NotificationHelper.cancelTaskReminder(context, taskId)
                        NotificationHelper.cancelPersistentNotification(context)

                        // Update widgets
                        updateAllWidgets(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_COMPLETE_TASK = "com.example.ACTION_WIDGET_COMPLETE_TASK"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TaskFlowWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (appWidgetIds.isNotEmpty()) {
                updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
            }
        }

        private fun updateWidgetsInternal(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val nextTaskWithSubtasks = db.taskDao().getNextUpcomingDeadlineSync()

                for (widgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_taskflow)

                    // Intent to open MainActivity on click
                    val openAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val openAppPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

                    if (nextTaskWithSubtasks != null) {
                        val task = nextTaskWithSubtasks.task
                        val category = db.categoryDao().getCategoryById(task.categoryId)

                        views.setTextViewText(R.id.widget_task_title, task.title)
                        views.setViewVisibility(R.id.widget_btn_complete, View.VISIBLE)

                        val timeString = formatWidgetDeadline(task.deadlineTimestamp)
                        views.setTextViewText(R.id.widget_deadline_time, timeString)

                        if (category != null) {
                            views.setViewVisibility(R.id.widget_category_pill, View.VISIBLE)
                            views.setTextViewText(R.id.widget_category_pill, category.name)
                        } else {
                            views.setViewVisibility(R.id.widget_category_pill, View.GONE)
                        }

                        // Complete button intent
                        val completeIntent = Intent(context, TaskFlowWidgetProvider::class.java).apply {
                            action = ACTION_WIDGET_COMPLETE_TASK
                            putExtra(NotificationHelper.EXTRA_TASK_ID, task.id)
                        }
                        val completePendingIntent = PendingIntent.getBroadcast(
                            context,
                            task.id.toInt(),
                            completeIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_btn_complete, completePendingIntent)

                        val isOverdue = task.deadlineTimestamp != null && System.currentTimeMillis() > task.deadlineTimestamp
                        if (isOverdue) {
                            views.setTextViewText(R.id.widget_status_badge, "Overdue!")
                        } else {
                            views.setTextViewText(R.id.widget_status_badge, "Next Due")
                        }
                    } else {
                        views.setTextViewText(R.id.widget_task_title, "No pending deadlines")
                        views.setTextViewText(R.id.widget_deadline_time, "All caught up! Great work 🎉")
                        views.setTextViewText(R.id.widget_status_badge, "Clear")
                        views.setViewVisibility(R.id.widget_category_pill, View.GONE)
                        views.setViewVisibility(R.id.widget_btn_complete, View.GONE)
                    }

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }
        }

        private fun formatWidgetDeadline(deadline: Long?): String {
            if (deadline == null) return "No deadline"
            val diff = deadline - System.currentTimeMillis()
            if (diff < 0) {
                val overdueHours = (-diff / (3600 * 1000)).toInt()
                return if (overdueHours == 0) "Overdue by < 1 hour" else "Overdue by ${overdueHours}h"
            }
            val mins = (diff / (60 * 1000)).toInt()
            val hours = mins / 60
            return when {
                mins < 60 -> "Due in $mins min"
                hours < 24 -> "Due in $hours hrs"
                else -> SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault()).format(Date(deadline))
            }
        }
    }
}
