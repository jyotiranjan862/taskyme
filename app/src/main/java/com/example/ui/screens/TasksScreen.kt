package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.repository.AppThemeMode
import com.example.ui.components.NextDeadlineHero
import com.example.ui.components.PlayfulEmptyState
import com.example.ui.components.TaskCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.PriorityUrgent
import com.example.ui.viewmodel.TaskFilter
import com.example.ui.viewmodel.TaskFlowUiState
import com.example.ui.viewmodel.TaskSort
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TasksScreen(
    uiState: TaskFlowUiState,
    themeMode: AppThemeMode,
    onToggleTheme: () -> Unit,
    onSelectFilter: (TaskFilter) -> Unit,
    onSelectCategory: (Long?) -> Unit,
    onSelectSort: (TaskSort) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleTask: (taskId: Long, isCompleted: Boolean) -> Unit,
    onToggleSubtask: (subtaskId: Long, isCompleted: Boolean) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onRescheduleTask: (taskId: Long) -> Unit,
    onDeleteTask: (taskId: Long) -> Unit,
    onRestoreTask: ((taskId: Long) -> Unit)? = null,
    onEmptyArchive: (() -> Unit)? = null,
    onQuickAddQuest: ((title: String, categoryName: String, priority: Priority) -> Unit)? = null,
    onCreateTaskClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val todayProgress = remember(uiState.todayCompletedCount, uiState.todayTotalCount) {
        if (uiState.todayTotalCount > 0) {
            uiState.todayCompletedCount.toFloat() / uiState.todayTotalCount.toFloat()
        } else 0f
    }
    val animatedTodayProgress by animateFloatAsState(
        targetValue = todayProgress,
        label = "todayProgress"
    )

    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTaskClick,
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .testTag("create_task_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Task",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Minimal Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TaskFlow",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = todayDateFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("search_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("sort_menu_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            TaskSort.entries.forEach { sort ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sort.title,
                                            fontWeight = if (uiState.sortOption == sort) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        onSelectSort(sort)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Persistent Streak & Progress Header across all tabs (no layout shift)
            val rawStreak = uiState.analyticsSummary?.currentStreak ?: 0
            val streak = if (rawStreak > 0) rawStreak else if (uiState.completedTaskCount > 0) 1 else 0
            val streakText = if (streak > 0) "${streak}d streak" else "0d streak"

            val progressText = if (uiState.todayTotalCount > 0) {
                "${uiState.todayCompletedCount}/${uiState.todayTotalCount} today (${(todayProgress * 100).toInt()}%)"
            } else if (uiState.completedTaskCount + uiState.activeTaskCount > 0) {
                "${uiState.completedTaskCount} completed • ${uiState.activeTaskCount} active"
            } else {
                "0/0 today (0%)"
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("streak_display_badge")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = CyanAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = streakText,
                            fontSize = 11.sp,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = {
                        if (uiState.todayTotalCount > 0) {
                            animatedTodayProgress
                        } else if (uiState.completedTaskCount + uiState.activeTaskCount > 0) {
                            uiState.completedTaskCount.toFloat() / (uiState.completedTaskCount + uiState.activeTaskCount).toFloat()
                        } else 0f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = if (todayProgress >= 1f && uiState.todayTotalCount > 0) MintSecondary else IndigoPrimary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Expandable Search Bar
            AnimatedVisibility(visible = isSearchExpanded) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search tasks...", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                        .testTag("task_search_field")
                )
            }

            // Minimal 3-tab filter strip: Tasks, Completed, Archive
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskFilter.entries.forEach { filter ->
                    val isSelected = uiState.selectedFilter == filter
                    val countText = when (filter) {
                        TaskFilter.TASKS -> if (uiState.activeTaskCount > 0) " (${uiState.activeTaskCount})" else ""
                        TaskFilter.COMPLETED -> if (uiState.completedTaskCount > 0) " (${uiState.completedTaskCount})" else ""
                        TaskFilter.ARCHIVE -> if (uiState.archivedTaskCount > 0) " (${uiState.archivedTaskCount})" else ""
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable {
                                onSelectCategory(null)
                                onSelectFilter(filter)
                            }
                            .testTag("filter_tab_${filter.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${filter.title}$countText",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            if (filter == TaskFilter.TASKS && uiState.lateTaskCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else PriorityUrgent)
                                )
                            }
                        }
                    }
                }
            }

            // Main Task Content: Centered Playful Empty State or Task List
            if (uiState.filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PlayfulEmptyState(
                        currentFilter = uiState.selectedFilter
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
                ) {
                    // Next deadline hero card (minimal banner in Tasks tab)
                    if (uiState.nextDeadlineTask != null &&
                        uiState.selectedFilter == TaskFilter.TASKS &&
                        uiState.searchQuery.isBlank() &&
                        uiState.selectedCategoryId == null
                    ) {
                        item(key = "next_deadline_hero") {
                            NextDeadlineHero(
                                taskItem = uiState.nextDeadlineTask,
                                onComplete = { onToggleTask(it, true) },
                                onReschedule = { onRescheduleTask(it) },
                                onCardClick = { onEditTask(it) }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }

                    // Archive header banner
                    if (uiState.selectedFilter == TaskFilter.ARCHIVE) {
                        item(key = "archive_header_banner") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Swipe right to restore, or left to delete forever.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (onEmptyArchive != null) {
                                    Text(
                                        text = "Empty Archive",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PriorityUrgent,
                                        modifier = Modifier
                                            .clickable { onEmptyArchive() }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(uiState.filteredTasks, key = { it.task.id }) { taskItem ->
                        TaskCard(
                            taskItem = taskItem,
                            onToggleCompletion = { onToggleTask(taskItem.task.id, it) },
                            onToggleSubtask = { subtaskId, isCompleted ->
                                onToggleSubtask(subtaskId, isCompleted)
                            },
                            onEdit = { onEditTask(taskItem) },
                            onReschedule = { onRescheduleTask(taskItem.task.id) },
                            onDelete = { onDeleteTask(taskItem.task.id) },
                            onRestore = { onRestoreTask?.invoke(taskItem.task.id) }
                        )
                    }
                }
            }
        }
    }
}
