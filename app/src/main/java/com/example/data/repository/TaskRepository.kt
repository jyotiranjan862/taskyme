package com.example.data.repository

import android.content.Context
import com.example.data.dao.AchievementDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.TaskDao
import com.example.data.model.AchievementEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.SubtaskEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskItem
import com.example.data.model.TaskWithSubtasks
import com.example.notification.NotificationHelper
import com.example.widget.TaskFlowWidgetProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class TaskRepository(
    private val context: Context,
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao,
    private val achievementDao: AchievementDao
) {

    val tasksFlow: Flow<List<TaskItem>> = combine(
        taskDao.getAllTasksWithSubtasksFlow(),
        categoryDao.getAllCategoriesFlow()
    ) { tasksWithSubtasks, categories ->
        val categoryMap = categories.associateBy { it.id }
        tasksWithSubtasks.map { tws ->
            TaskItem(
                task = tws.task,
                category = categoryMap[tws.task.categoryId],
                subtasks = tws.subtasks
            )
        }
    }

    val categoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    val achievementsFlow: Flow<List<AchievementEntity>> = achievementDao.getAllAchievementsFlow()

    suspend fun createTask(
        task: TaskEntity,
        subtaskTitles: List<String>
    ): Long {
        val taskId = taskDao.insertTask(task)
        if (subtaskTitles.isNotEmpty()) {
            val subtasks = subtaskTitles.filter { it.isNotBlank() }.mapIndexed { index, title ->
                SubtaskEntity(taskId = taskId, title = title, position = index)
            }
            taskDao.insertSubtasks(subtasks)
        }

        // Schedule reminder if configured
        val savedTask = task.copy(id = taskId)
        NotificationHelper.scheduleTaskReminder(context, savedTask)

        // Refresh system integrations
        refreshWidgetAndNotification()

        // Check category count achievement
        evaluateCategoryAchievement()

        return taskId
    }

    suspend fun updateTask(
        task: TaskEntity,
        subtasks: List<SubtaskEntity>
    ) {
        taskDao.updateTask(task)
        taskDao.deleteSubtasksForTask(task.id)
        taskDao.insertSubtasks(subtasks)

        if (!task.isCompleted) {
            NotificationHelper.scheduleTaskReminder(context, task)
        } else {
            NotificationHelper.cancelTaskReminder(context, task.id)
        }

        refreshWidgetAndNotification()
    }

    suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        val now = if (isCompleted) System.currentTimeMillis() else null
        taskDao.updateTaskCompletion(taskId, isCompleted, now)

        val task = taskDao.getTaskById(taskId)
        if (task != null) {
            if (isCompleted) {
                NotificationHelper.cancelTaskReminder(context, taskId)
                // If this is a recurring task, create next instance
                handleRecurringTask(task)
            } else {
                NotificationHelper.scheduleTaskReminder(context, task)
            }
        }

        refreshWidgetAndNotification()
        evaluateAchievements()
    }

    suspend fun toggleSubtaskCompletion(subtaskId: Long, isCompleted: Boolean) {
        taskDao.updateSubtaskCompletion(subtaskId, isCompleted)
        evaluateSubtaskAchievement()
    }

    suspend fun deleteTask(taskId: Long) {
        NotificationHelper.cancelTaskReminder(context, taskId)
        taskDao.deleteTaskById(taskId)
        refreshWidgetAndNotification()
    }

    suspend fun rescheduleTask(taskId: Long, newDeadline: Long?, hasSpecificTime: Boolean) {
        taskDao.rescheduleTask(taskId, newDeadline, hasSpecificTime)
        val updatedTask = taskDao.getTaskById(taskId)
        if (updatedTask != null) {
            NotificationHelper.scheduleTaskReminder(context, updatedTask)
        }
        refreshWidgetAndNotification()
    }

    suspend fun addCategory(category: CategoryEntity): Long {
        val id = categoryDao.insertCategory(category)
        evaluateCategoryAchievement()
        return id
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun refreshWidgetAndNotification() {
        TaskFlowWidgetProvider.updateAllWidgets(context)

        val nextTaskWithSubtasks = taskDao.getNextUpcomingDeadlineSync()
        if (nextTaskWithSubtasks != null && nextTaskWithSubtasks.task.isPersistentNotificationEnabled) {
            val cat = categoryDao.getCategoryById(nextTaskWithSubtasks.task.categoryId)
            NotificationHelper.showPersistentDeadlineNotification(context, nextTaskWithSubtasks.task, cat)
        } else {
            NotificationHelper.cancelPersistentNotification(context)
        }
    }

    private suspend fun handleRecurringTask(completedTask: TaskEntity) {
        val rule = completedTask.recurringRule ?: return
        val currentDeadline = completedTask.deadlineTimestamp ?: return

        val cal = Calendar.getInstance()
        cal.timeInMillis = currentDeadline

        when (rule) {
            "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKDAYS" -> {
                do {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                } while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            }
            "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> cal.add(Calendar.MONTH, 1)
            else -> return
        }

        val nextTask = completedTask.copy(
            id = 0,
            isCompleted = false,
            completedAt = null,
            deadlineTimestamp = cal.timeInMillis,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newId = taskDao.insertTask(nextTask)
        NotificationHelper.scheduleTaskReminder(context, nextTask.copy(id = newId))
    }

    private suspend fun evaluateAchievements() {
        val allTasks = taskDao.getAllTasksSync()
        val completedTasks = allTasks.filter { it.isCompleted }
        val now = System.currentTimeMillis()

        // 1. First Task
        if (completedTasks.isNotEmpty()) {
            unlockAchievement("first_task", 1, 1, now)
        }

        // 2. Getting Started (5 tasks)
        val completedCount = completedTasks.size
        unlockAchievement("getting_started", completedCount.coerceAtMost(5), 5, now)

        // 3. Century Achiever (100 tasks)
        unlockAchievement("monthly_achiever", completedCount.coerceAtMost(100), 100, now)

        // 4. Deadline Hero (10 tasks on or before deadline)
        val onTimeCount = completedTasks.count { task ->
            val deadline = task.deadlineTimestamp
            val completed = task.completedAt
            deadline != null && completed != null && completed <= deadline
        }
        unlockAchievement("deadline_hero", onTimeCount.coerceAtMost(10), 10, now)

        // 5. Focus Champion (10 tasks in one day)
        val tasksPerDay = completedTasks.groupBy { task ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = task.completedAt ?: task.updatedAt
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        }
        val maxInOneDay = tasksPerDay.values.maxOfOrNull { it.size } ?: 0
        unlockAchievement("focus_champion", maxInOneDay.coerceAtMost(10), 10, now)

        // 6. Streak (consecutive days)
        val streak = calculateStreak(completedTasks)
        unlockAchievement("consistent_3", streak.coerceAtMost(3), 3, now)
        unlockAchievement("week_streak", streak.coerceAtMost(7), 7, now)

        // 7. Weekly Goal
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val completedThisWeek = completedTasks.count { (it.completedAt ?: 0L) >= startOfWeek }
        if (completedThisWeek >= 7) {
            unlockAchievement("weekly_goal", 1, 1, now)
        }
    }

    private suspend fun evaluateSubtaskAchievement() {
        val allTasks = taskDao.getAllTasksSync()
        var completedSubtasks = 0
        for (task in allTasks) {
            val tws = taskDao.getTaskWithSubtasksById(task.id)
            if (tws != null) {
                completedSubtasks += tws.subtasks.count { it.isCompleted }
            }
        }
        unlockAchievement("subtask_slayer", completedSubtasks.coerceAtMost(15), 15, System.currentTimeMillis())
    }

    private suspend fun evaluateCategoryAchievement() {
        val allTasks = taskDao.getAllTasksSync()
        val distinctCategories = allTasks.map { it.categoryId }.distinct().size
        unlockAchievement("organized_mind", distinctCategories.coerceAtMost(4), 4, System.currentTimeMillis())
    }

    private suspend fun unlockAchievement(id: String, progress: Int, max: Int, timestamp: Long) {
        val isUnlocked = progress >= max
        achievementDao.updateProgress(
            id = id,
            progress = progress,
            unlocked = isUnlocked,
            unlockedAt = if (isUnlocked) timestamp else null
        )
    }

    private fun calculateStreak(completedTasks: List<TaskEntity>): Int {
        if (completedTasks.isEmpty()) return 0
        val daySet = completedTasks.mapNotNull { it.completedAt }.map { time ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = time
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet()

        var currentStreak = 0
        val checkCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Check if today or yesterday has a completed task
        var checkTime = checkCal.timeInMillis
        if (!daySet.contains(checkTime)) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            checkTime = checkCal.timeInMillis
            if (!daySet.contains(checkTime)) {
                return 0
            }
        }

        while (daySet.contains(checkTime)) {
            currentStreak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            checkTime = checkCal.timeInMillis
        }
        return currentStreak
    }
}
