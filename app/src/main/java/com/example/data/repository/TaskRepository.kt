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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskRepository(
    private val context: Context,
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao,
    private val achievementDao: AchievementDao
) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            achievementDao.insertAchievements(AchievementEntity.defaultAchievements())
            evaluateAchievements()
        }
    }

    val tasksFlow: Flow<List<TaskItem>> = combine(
        taskDao.getAllTasksWithSubtasksFlow(),
        categoryDao.getAllCategoriesFlow()
    ) { tasksWithSubtasks, categories ->
        val categoryMap = categories.associateBy { it.id }
        tasksWithSubtasks
            .filter { tws ->
                val title = tws.task.title
                !title.contains("Q4 Strategy", ignoreCase = true) &&
                !title.contains("Morning High-Intensity", ignoreCase = true) &&
                !title.contains("Clean Architecture", ignoreCase = true) &&
                !title.contains("Install TaskFlow", ignoreCase = true)
            }
            .map { tws ->
                TaskItem(
                    task = tws.task,
                    category = categoryMap[tws.task.categoryId],
                    subtasks = tws.subtasks
                )
            }
    }

    suspend fun cleanupLegacyDemoTasks() {
        taskDao.deleteDemoTasks()
        refreshWidgetAndNotification()
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
        archiveTask(taskId)
    }

    suspend fun archiveTask(taskId: Long) {
        NotificationHelper.cancelTaskReminder(context, taskId)
        taskDao.updateTaskArchived(taskId, isArchived = true)
        refreshWidgetAndNotification()
    }

    suspend fun unarchiveTask(taskId: Long) {
        taskDao.updateTaskArchived(taskId, isArchived = false)
        val task = taskDao.getTaskById(taskId)
        if (task != null && !task.isCompleted) {
            NotificationHelper.scheduleTaskReminder(context, task)
        }
        refreshWidgetAndNotification()
    }

    suspend fun permanentlyDeleteTask(taskId: Long) {
        NotificationHelper.cancelTaskReminder(context, taskId)
        taskDao.deleteTaskById(taskId)
        refreshWidgetAndNotification()
    }

    suspend fun emptyArchive() {
        taskDao.emptyArchive()
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
        val activeTasks = allTasks.filter { !it.isCompleted && !it.isArchived }
        val now = System.currentTimeMillis()
        val completedCount = completedTasks.size

        // 1. Task Completion Milestones (12 Badges)
        unlockAchievement("first_task", completedCount.coerceAtMost(1), 1, now)
        unlockAchievement("task_3", completedCount.coerceAtMost(3), 3, now)
        unlockAchievement("getting_started", completedCount.coerceAtMost(5), 5, now)
        unlockAchievement("task_10", completedCount.coerceAtMost(10), 10, now)
        unlockAchievement("task_20", completedCount.coerceAtMost(20), 20, now)
        unlockAchievement("task_35", completedCount.coerceAtMost(35), 35, now)
        unlockAchievement("task_50", completedCount.coerceAtMost(50), 50, now)
        unlockAchievement("task_75", completedCount.coerceAtMost(75), 75, now)
        unlockAchievement("monthly_achiever", completedCount.coerceAtMost(100), 100, now)
        unlockAchievement("task_150", completedCount.coerceAtMost(150), 150, now)
        unlockAchievement("task_250", completedCount.coerceAtMost(250), 250, now)
        unlockAchievement("task_500", completedCount.coerceAtMost(500), 500, now)

        // 2. Streaks & Consistency (10 Badges)
        val streak = calculateStreak(completedTasks)
        unlockAchievement("streak_2", streak.coerceAtMost(2), 2, now)
        unlockAchievement("consistent_3", streak.coerceAtMost(3), 3, now)
        unlockAchievement("streak_5", streak.coerceAtMost(5), 5, now)
        unlockAchievement("week_streak", streak.coerceAtMost(7), 7, now)
        unlockAchievement("streak_10", streak.coerceAtMost(10), 10, now)
        unlockAchievement("streak_14", streak.coerceAtMost(14), 14, now)
        unlockAchievement("streak_21", streak.coerceAtMost(21), 21, now)
        unlockAchievement("streak_30", streak.coerceAtMost(30), 30, now)
        unlockAchievement("streak_60", streak.coerceAtMost(60), 60, now)
        unlockAchievement("streak_100", streak.coerceAtMost(100), 100, now)

        // 3. Punctuality & Speed (6 Badges)
        val onTimeCount = completedTasks.count { task ->
            val deadline = task.deadlineTimestamp
            val completed = task.completedAt
            deadline != null && completed != null && completed <= deadline
        }
        unlockAchievement("on_time_first", onTimeCount.coerceAtMost(1), 1, now)
        unlockAchievement("deadline_hero", onTimeCount.coerceAtMost(10), 10, now)
        unlockAchievement("deadline_25", onTimeCount.coerceAtMost(25), 25, now)
        unlockAchievement("deadline_50", onTimeCount.coerceAtMost(50), 50, now)

        val hasSpeedDemon = completedTasks.any { task ->
            val deadline = task.deadlineTimestamp ?: 0L
            val completed = task.completedAt ?: 0L
            deadline > 0 && completed > 0 && (deadline - completed >= 12 * 3600 * 1000L)
        }
        unlockAchievement("speed_demon", if (hasSpeedDemon) 1 else 0, 1, now)

        val overdueActiveCount = activeTasks.count { task ->
            val d = task.deadlineTimestamp
            d != null && d < now
        }
        val zeroOverdueProgress = if (overdueActiveCount == 0 && completedCount >= 5) 5 else completedCount.coerceAtMost(4)
        unlockAchievement("zero_overdue", zeroOverdueProgress, 5, now)

        // 4. Daily Focus & Productivity Surges (6 Badges)
        val tasksPerDay = completedTasks.groupBy { task ->
            val cal = Calendar.getInstance().apply { timeInMillis = task.completedAt ?: task.updatedAt }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        }
        val maxInOneDay = tasksPerDay.values.maxOfOrNull { it.size } ?: 0
        unlockAchievement("busy_bee", maxInOneDay.coerceAtMost(3), 3, now)
        unlockAchievement("focus_champion", maxInOneDay.coerceAtMost(5), 5, now)
        unlockAchievement("power_day", maxInOneDay.coerceAtMost(10), 10, now)

        val hasEarlyBird = completedTasks.any { task ->
            val cal = Calendar.getInstance().apply { timeInMillis = task.completedAt ?: task.updatedAt }
            cal.get(Calendar.HOUR_OF_DAY) < 8
        }
        unlockAchievement("early_bird", if (hasEarlyBird) 1 else 0, 1, now)

        val hasNightOwl = completedTasks.any { task ->
            val cal = Calendar.getInstance().apply { timeInMillis = task.completedAt ?: task.updatedAt }
            cal.get(Calendar.HOUR_OF_DAY) >= 22
        }
        unlockAchievement("night_owl", if (hasNightOwl) 1 else 0, 1, now)

        val morningBlitzCount = tasksPerDay.values.maxOfOrNull { dayList ->
            dayList.count { task ->
                val cal = Calendar.getInstance().apply { timeInMillis = task.completedAt ?: task.updatedAt }
                cal.get(Calendar.HOUR_OF_DAY) in 5 until 12
            }
        } ?: 0
        unlockAchievement("morning_power", morningBlitzCount.coerceAtMost(3), 3, now)

        val weekendTasks = completedTasks.count { task ->
            val cal = Calendar.getInstance().apply { timeInMillis = task.completedAt ?: task.updatedAt }
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
        }
        unlockAchievement("weekend_warrior", weekendTasks.coerceAtMost(2), 2, now)

        // 5. Subtasks & Detailed Execution (5 Badges)
        var totalCompletedSubtasks = 0
        for (task in allTasks) {
            val tws = taskDao.getTaskWithSubtasksById(task.id)
            if (tws != null) {
                totalCompletedSubtasks += tws.subtasks.count { it.isCompleted }
            }
        }
        unlockAchievement("first_subtask", totalCompletedSubtasks.coerceAtMost(1), 1, now)
        unlockAchievement("subtask_5", totalCompletedSubtasks.coerceAtMost(5), 5, now)
        unlockAchievement("subtask_slayer", totalCompletedSubtasks.coerceAtMost(15), 15, now)
        unlockAchievement("subtask_30", totalCompletedSubtasks.coerceAtMost(30), 30, now)
        unlockAchievement("subtask_50", totalCompletedSubtasks.coerceAtMost(50), 50, now)

        // 6. Categories & Life Balance (5 Badges)
        val distinctCategories = allTasks.map { it.categoryId }.distinct().size
        unlockAchievement("organized_mind", distinctCategories.coerceAtMost(4), 4, now)

        val categories = categoryDao.getAllCategoriesSync()
        val catNameMap = categories.associate { it.id to it.name }
        val workCompleted = completedTasks.count { catNameMap[it.categoryId]?.equals("Work", ignoreCase = true) == true }
        val studyCompleted = completedTasks.count { catNameMap[it.categoryId]?.equals("Learning", ignoreCase = true) == true || catNameMap[it.categoryId]?.equals("Study", ignoreCase = true) == true }
        val healthCompleted = completedTasks.count { catNameMap[it.categoryId]?.equals("Health", ignoreCase = true) == true }
        val personalCompleted = completedTasks.count { catNameMap[it.categoryId]?.equals("Personal", ignoreCase = true) == true }

        unlockAchievement("work_pro", workCompleted.coerceAtMost(5), 5, now)
        unlockAchievement("study_master", studyCompleted.coerceAtMost(5), 5, now)
        unlockAchievement("health_champion", healthCompleted.coerceAtMost(5), 5, now)
        unlockAchievement("personal_growth", personalCompleted.coerceAtMost(5), 5, now)

        // 7. Priorities & Clean Slate (5 Badges)
        val urgentCompleted = completedTasks.count { it.priority.equals("URGENT", ignoreCase = true) }
        unlockAchievement("urgent_slayer", urgentCompleted.coerceAtMost(1), 1, now)
        unlockAchievement("urgent_5", urgentCompleted.coerceAtMost(5), 5, now)

        val highCompleted = completedTasks.count { it.priority.equals("HIGH", ignoreCase = true) }
        unlockAchievement("high_priority_10", highCompleted.coerceAtMost(10), 10, now)

        val distinctPriorities = completedTasks.map { it.priority.uppercase() }.distinct().size
        unlockAchievement("balanced_priorities", distinctPriorities.coerceAtMost(4), 4, now)

        val isInboxZero = activeTasks.isEmpty() && completedTasks.isNotEmpty()
        unlockAchievement("inbox_zero", if (isInboxZero) 1 else 0, 1, now)
    }

    private suspend fun evaluateSubtaskAchievement() {
        evaluateAchievements()
    }

    private suspend fun evaluateCategoryAchievement() {
        evaluateAchievements()
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
