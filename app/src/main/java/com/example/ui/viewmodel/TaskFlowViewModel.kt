package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.TaskFlowApplication
import com.example.data.model.AchievementEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.Priority
import com.example.data.model.SubtaskEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskItem
import com.example.data.repository.AppThemeMode
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.TaskRepository
import com.example.ui.analytics.AnalyticsCalculator
import com.example.ui.analytics.AnalyticsPeriodSummary
import com.example.ui.analytics.AnalyticsTimeframe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TaskFilter(val title: String) {
    TASKS("Tasks"),
    COMPLETED("Completed"),
    ARCHIVE("Archive")
}

enum class TaskSort(val title: String) {
    DEADLINE("Deadline"),
    PRIORITY("Priority"),
    NEWEST("Newest"),
    TITLE("Alphabetical")
}

data class TaskFlowUiState(
    val rawTasks: List<TaskItem> = emptyList(),
    val filteredTasks: List<TaskItem> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val achievements: List<AchievementEntity> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.TASKS,
    val selectedCategoryId: Long? = null,
    val selectedPriority: Priority? = null,
    val searchQuery: String = "",
    val sortOption: TaskSort = TaskSort.DEADLINE,
    val nextDeadlineTask: TaskItem? = null,
    val todayCompletedCount: Int = 0,
    val todayTotalCount: Int = 0,
    val lateTaskCount: Int = 0,
    val archivedTaskCount: Int = 0,
    val completedTaskCount: Int = 0,
    val activeTaskCount: Int = 0,
    val analyticsTimeframe: AnalyticsTimeframe = AnalyticsTimeframe.LAST_7_DAYS,
    val analyticsSummary: AnalyticsPeriodSummary? = null,
    val newlyUnlockedAchievement: AchievementEntity? = null
)

class TaskFlowViewModel(
    application: Application,
    private val taskRepository: TaskRepository,
    private val preferencesRepository: PreferencesRepository
) : AndroidViewModel(application) {

    val themeMode: StateFlow<AppThemeMode> = preferencesRepository.themeMode

    private val _selectedFilter = MutableStateFlow(TaskFilter.TASKS)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    val selectedPriority = _selectedPriority.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(TaskSort.DEADLINE)
    val sortOption = _sortOption.asStateFlow()

    private val _analyticsTimeframe = MutableStateFlow(AnalyticsTimeframe.LAST_7_DAYS)
    val analyticsTimeframe = _analyticsTimeframe.asStateFlow()

    private val _newlyUnlockedAchievement = MutableStateFlow<AchievementEntity?>(null)
    val newlyUnlockedAchievement = _newlyUnlockedAchievement.asStateFlow()

    val uiState: StateFlow<TaskFlowUiState> = combine(
        taskRepository.tasksFlow,
        taskRepository.categoriesFlow,
        taskRepository.achievementsFlow,
        _selectedFilter,
        _selectedCategoryId,
        _selectedPriority,
        _searchQuery,
        _sortOption,
        _analyticsTimeframe
    ) { args: Array<Any?> ->
        val tasks = args[0] as List<TaskItem>
        val categories = args[1] as List<CategoryEntity>
        val achievements = args[2] as List<AchievementEntity>
        val filter = args[3] as TaskFilter
        val categoryId = args[4] as? Long
        val priority = args[5] as? Priority
        val query = args[6] as String
        val sort = args[7] as TaskSort
        val timeframe = args[8] as AnalyticsTimeframe

        // Calculate today progress
        val now = System.currentTimeMillis()
        val startOfToday = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfToday = startOfToday + (24 * 60 * 60 * 1000)

        val activeTasks = tasks.filter { !it.task.isArchived && !it.task.isCompleted }
        val completedTasks = tasks.filter { !it.task.isArchived && it.task.isCompleted }
        val archivedTasks = tasks.filter { it.task.isArchived }
        val lateCount = activeTasks.count { it.isOverdue }

        val todayTasks = tasks.filter {
            !it.task.isArchived && it.task.deadlineTimestamp != null && it.task.deadlineTimestamp in startOfToday until endOfToday
        }
        val todayCompleted = todayTasks.count { it.task.isCompleted }
        val todayTotal = todayTasks.size

        // Next upcoming deadline task (not archived, not completed, has deadline)
        val nextDeadlineTask = activeTasks
            .filter { it.task.deadlineTimestamp != null }
            .minByOrNull { it.task.deadlineTimestamp!! }

        // Primary tab filter
        var filtered = when (filter) {
            TaskFilter.TASKS -> activeTasks
            TaskFilter.COMPLETED -> completedTasks
            TaskFilter.ARCHIVE -> archivedTasks
        }

        // Category filter
        if (categoryId != null) {
            filtered = filtered.filter { it.task.categoryId == categoryId }
        }

        // Priority filter
        if (priority != null) {
            filtered = filtered.filter { it.task.priority.equals(priority.name, ignoreCase = true) }
        }

        // Search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.task.title.lowercase().contains(q) ||
                        it.task.description.lowercase().contains(q) ||
                        it.task.tags.lowercase().contains(q) ||
                        (it.category?.name?.lowercase()?.contains(q) == true)
            }
        }

        // Sort tasks
        filtered = when (sort) {
            TaskSort.DEADLINE -> filtered.sortedWith(
                compareBy<TaskItem> { it.task.deadlineTimestamp ?: Long.MAX_VALUE }
                    .thenByDescending { it.task.createdAt }
            )
            TaskSort.PRIORITY -> filtered.sortedWith(
                compareByDescending<TaskItem> { Priority.fromString(it.task.priority).level }
                    .thenBy { it.task.deadlineTimestamp ?: Long.MAX_VALUE }
            )
            TaskSort.NEWEST -> filtered.sortedByDescending { it.task.createdAt }
            TaskSort.TITLE -> filtered.sortedBy { it.task.title.lowercase() }
        }

        // Calculate analytics (excluding archived tasks)
        val analyticsSummary = AnalyticsCalculator.computeSummary(tasks.filter { !it.task.isArchived }, categories, timeframe)

        TaskFlowUiState(
            rawTasks = tasks,
            filteredTasks = filtered,
            categories = categories,
            achievements = achievements,
            selectedFilter = filter,
            selectedCategoryId = categoryId,
            selectedPriority = priority,
            searchQuery = query,
            sortOption = sort,
            nextDeadlineTask = nextDeadlineTask,
            todayCompletedCount = todayCompleted,
            todayTotalCount = todayTotal,
            lateTaskCount = lateCount,
            archivedTaskCount = archivedTasks.size,
            completedTaskCount = completedTasks.size,
            activeTaskCount = activeTasks.size,
            analyticsTimeframe = timeframe,
            analyticsSummary = analyticsSummary,
            newlyUnlockedAchievement = _newlyUnlockedAchievement.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskFlowUiState()
    )

    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    fun setCategoryFilter(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun setPriorityFilter(priority: Priority?) {
        _selectedPriority.value = priority
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: TaskSort) {
        _sortOption.value = sort
    }

    fun setAnalyticsTimeframe(timeframe: AnalyticsTimeframe) {
        _analyticsTimeframe.value = timeframe
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferencesRepository.setThemeMode(mode)
    }

    fun dismissAchievementCelebration() {
        _newlyUnlockedAchievement.value = null
    }

    fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(taskId, isCompleted)
        }
    }

    fun toggleSubtaskCompletion(subtaskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleSubtaskCompletion(subtaskId, isCompleted)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            when (_selectedFilter.value) {
                TaskFilter.TASKS -> taskRepository.archiveTask(taskId)
                TaskFilter.COMPLETED, TaskFilter.ARCHIVE -> taskRepository.permanentlyDeleteTask(taskId)
            }
        }
    }

    fun archiveTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.archiveTask(taskId)
        }
    }

    fun unarchiveTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.unarchiveTask(taskId)
        }
    }

    fun permanentlyDeleteTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.permanentlyDeleteTask(taskId)
        }
    }

    fun emptyArchive() {
        viewModelScope.launch {
            taskRepository.emptyArchive()
        }
    }

    fun rescheduleTask(taskId: Long, newDeadline: Long?, hasSpecificTime: Boolean) {
        viewModelScope.launch {
            taskRepository.rescheduleTask(taskId, newDeadline, hasSpecificTime)
        }
    }

    fun createTask(
        title: String,
        description: String,
        categoryId: Long,
        priority: Priority,
        deadlineTimestamp: Long?,
        hasSpecificTime: Boolean,
        reminderMinutesBefore: Int,
        isPersistentNotification: Boolean,
        recurringRule: String?,
        estimatedDurationMinutes: Int,
        tags: String,
        subtasks: List<String>
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                categoryId = categoryId,
                priority = priority.name,
                deadlineTimestamp = deadlineTimestamp,
                hasSpecificTime = hasSpecificTime,
                reminderMinutesBefore = reminderMinutesBefore,
                isPersistentNotificationEnabled = isPersistentNotification,
                recurringRule = recurringRule,
                estimatedDurationMinutes = estimatedDurationMinutes,
                tags = tags.trim()
            )
            taskRepository.createTask(task, subtasks)
        }
    }

    fun updateTask(
        task: TaskEntity,
        subtasks: List<SubtaskEntity>
    ) {
        viewModelScope.launch {
            taskRepository.updateTask(task, subtasks)
        }
    }

    fun createCategory(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            taskRepository.addCategory(
                CategoryEntity(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            taskRepository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            taskRepository.deleteCategory(category)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as TaskFlowApplication
                    return TaskFlowViewModel(
                        application = app,
                        taskRepository = app.taskRepository,
                        preferencesRepository = app.preferencesRepository
                    ) as T
                }
            }
    }
}
