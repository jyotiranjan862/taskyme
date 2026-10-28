package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskItem
import com.example.data.repository.AppThemeMode
import com.example.ui.components.AchievementCelebrationDialog
import com.example.ui.components.CategoryManagementDialog
import com.example.ui.components.RescheduleDialog
import com.example.ui.components.TaskCreateEditSheet
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TaskFlowTheme
import com.example.ui.viewmodel.TaskFlowViewModel

sealed class AppNavDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Tasks : AppNavDestination("tasks", "Tasks", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt)
    data object Timeline : AppNavDestination("timeline", "Timeline", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    data object Analytics : AppNavDestination("analytics", "Analytics", Icons.Filled.Insights, Icons.Outlined.Insights)
    data object Achievements : AppNavDestination("achievements", "Badges", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
}

class MainActivity : ComponentActivity() {

    private val viewModel: TaskFlowViewModel by viewModels {
        TaskFlowViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            TaskFlowTheme(darkTheme = isDarkTheme) {
                MainAppContent(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onToggleTheme = {
                        val nextMode = when (themeMode) {
                            AppThemeMode.DARK -> AppThemeMode.LIGHT
                            AppThemeMode.LIGHT -> AppThemeMode.SYSTEM
                            AppThemeMode.SYSTEM -> AppThemeMode.DARK
                        }
                        viewModel.setThemeMode(nextMode)
                    }
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: TaskFlowViewModel,
    themeMode: AppThemeMode,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Notification permission request for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ -> }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var currentTab by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            AppNavDestination.Tasks,
            AppNavDestination.Timeline,
            AppNavDestination.Analytics,
            AppNavDestination.Achievements
        )
    }

    // Modal dialogs state
    var showCreateSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }
    var taskToRescheduleId by remember { mutableStateOf<Long?>(null) }
    var showCategoryManagement by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_bottom_nav")
            ) {
                tabs.forEachIndexed { index, dest ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.title
                            )
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            selectedTextColor = IndigoPrimary,
                            indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_${dest.route}")
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> TasksScreen(
                    uiState = uiState,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    onSelectFilter = { viewModel.setFilter(it) },
                    onSelectCategory = { viewModel.setCategoryFilter(it) },
                    onSelectSort = { viewModel.setSortOption(it) },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onToggleTask = { id, done -> viewModel.toggleTaskCompletion(id, done) },
                    onToggleSubtask = { id, done -> viewModel.toggleSubtaskCompletion(id, done) },
                    onEditTask = {
                        taskToEdit = it
                        showCreateSheet = true
                    },
                    onRescheduleTask = { taskToRescheduleId = it },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onCreateTaskClick = {
                        taskToEdit = null
                        showCreateSheet = true
                    },
                    onManageCategoriesClick = { showCategoryManagement = true }
                )

                1 -> TimelineScreen(
                    tasks = uiState.rawTasks,
                    onToggleTask = { id, done -> viewModel.toggleTaskCompletion(id, done) },
                    onToggleSubtask = { id, done -> viewModel.toggleSubtaskCompletion(id, done) },
                    onEditTask = {
                        taskToEdit = it
                        showCreateSheet = true
                    },
                    onRescheduleTask = { taskToRescheduleId = it },
                    onDeleteTask = { viewModel.deleteTask(it) }
                )

                2 -> AnalyticsScreen(
                    summary = uiState.analyticsSummary,
                    currentTimeframe = uiState.analyticsTimeframe,
                    onSelectTimeframe = { viewModel.setAnalyticsTimeframe(it) }
                )

                3 -> AchievementsScreen(
                    achievements = uiState.achievements
                )
            }
        }
    }

    // Task Creation & Editing Bottom Sheet
    if (showCreateSheet) {
        TaskCreateEditSheet(
            taskToEdit = taskToEdit,
            categories = uiState.categories,
            onDismiss = {
                showCreateSheet = false
                taskToEdit = null
            },
            onSaveTask = { title, desc, catId, prio, deadline, hasTime, rem, persistent, recurring, dur, tags, subtasks ->
                viewModel.createTask(
                    title, desc, catId, prio, deadline, hasTime, rem, persistent, recurring, dur, tags, subtasks
                )
            },
            onUpdateExistingTask = { updatedTask, updatedSubtasks ->
                viewModel.updateTask(updatedTask, updatedSubtasks)
            },
            onAddNewCategoryRequested = {
                showCategoryManagement = true
            }
        )
    }

    // Reschedule Dialog
    taskToRescheduleId?.let { taskId ->
        RescheduleDialog(
            taskId = taskId,
            onDismiss = { taskToRescheduleId = null },
            onReschedule = { newDeadline, hasTime ->
                viewModel.rescheduleTask(taskId, newDeadline, hasTime)
            }
        )
    }

    // Category Management Dialog
    if (showCategoryManagement) {
        CategoryManagementDialog(
            categories = uiState.categories,
            onDismiss = { showCategoryManagement = false },
            onCreateCategory = { name, color, icon ->
                viewModel.createCategory(name, color, icon)
            },
            onDeleteCategory = { cat ->
                viewModel.deleteCategory(cat)
            }
        )
    }

    // Achievement Celebration Unlock Popup
    AchievementCelebrationDialog(
        achievement = uiState.newlyUnlockedAchievement,
        onDismiss = { viewModel.dismissAchievementCelebration() }
    )
}
