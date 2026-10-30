package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.ui.components.TaskCard
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayItem(
    val dayOfWeek: String, // e.g. "WED"
    val dayOfMonth: String, // e.g. "28"
    val fullDateLabel: String, // e.g. "WEDNESDAY, 28 OCTOBER"
    val relativeLabel: String, // e.g. "Today", "Tomorrow", "In 2 days"
    val timestamp: Long,
    val offset: Int,
    val hasTasks: Boolean
)

@Composable
fun TimelineScreen(
    tasks: List<TaskItem>,
    onToggleTask: (taskId: Long, isCompleted: Boolean) -> Unit,
    onToggleSubtask: (subtaskId: Long, isCompleted: Boolean) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onRescheduleTask: (taskId: Long) -> Unit,
    onDeleteTask: (taskId: Long) -> Unit,
    onCreateTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableIntStateOf(0) }

    // Generate 7 upcoming days for the calendar strip matching Screen 1 from mockup
    val calendarDays = remember(tasks) {
        val list = mutableListOf<CalendarDayItem>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val numFormat = SimpleDateFormat("d", Locale.getDefault())
        val fullFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

        for (i in 0..6) {
            val startOfDay = cal.timeInMillis
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000)
            val hasTasks = tasks.any {
                val d = it.task.deadlineTimestamp
                d != null && d in startOfDay until endOfDay
            }
            val relLabel = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                2 -> "In 2 days"
                3 -> "In 3 days"
                else -> "In $i days"
            }
            list.add(
                CalendarDayItem(
                    dayOfWeek = dayFormat.format(cal.time).uppercase(),
                    dayOfMonth = numFormat.format(cal.time),
                    fullDateLabel = fullFormat.format(cal.time).uppercase(),
                    relativeLabel = relLabel,
                    timestamp = startOfDay,
                    offset = i,
                    hasTasks = hasTasks
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Group tasks by day
    val groupedDays = remember(tasks, selectedDayIndex) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayGroups = mutableListOf<Pair<CalendarDayItem, List<TaskItem>>>()

        calendarDays.forEachIndexed { idx, dayItem ->
            val start = dayItem.timestamp
            val end = start + (24 * 60 * 60 * 1000)
            val dayTasks = tasks.filter {
                val d = it.task.deadlineTimestamp
                d != null && d in start until end
            }.sortedBy { it.task.deadlineTimestamp }

            // If a specific day is tapped in the strip, show that day first, or show upcoming groups
            if (dayTasks.isNotEmpty() || idx == selectedDayIndex) {
                dayGroups.add(Pair(dayItem, dayTasks))
            }
        }
        dayGroups
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
                    .testTag("timeline_create_fab")
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
            // Header Bar matching Screen 1 mockup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Timeline",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Deadlines in chronological order",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Horizontal Calendar Date Strip (WED 28, THU 29, etc.)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(calendarDays) { day ->
                    val isSelected = selectedDayIndex == day.offset

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surface,
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)) else null,
                        tonalElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier
                            .width(54.dp)
                            .height(72.dp)
                            .clickable { selectedDayIndex = day.offset }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = day.dayOfWeek,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = day.dayOfMonth,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )

                            // Tiny indicator dot
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> Color.White
                                            day.hasTasks -> IndigoPrimary
                                            else -> Color.Transparent
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grouped Task Sections
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (groupedDays.all { it.second.isEmpty() }) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No deadlines scheduled",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+' to schedule a task deadline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    groupedDays.forEach { (dayItem, dayTasks) ->
                        if (dayTasks.isNotEmpty()) {
                            item(key = "header_${dayItem.timestamp}") {
                                // Section Date Header (e.g. WEDNESDAY, 28 OCTOBER  [Today])
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dayItem.fullDateLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = IndigoPrimary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = dayItem.relativeLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IndigoPrimary,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            items(dayTasks, key = { it.task.id }) { taskItem ->
                                TaskCard(
                                    taskItem = taskItem,
                                    onToggleCompletion = { onToggleTask(taskItem.task.id, it) },
                                    onToggleSubtask = { subtaskId, isCompleted ->
                                        onToggleSubtask(subtaskId, isCompleted)
                                    },
                                    onEdit = { onEditTask(taskItem) },
                                    onReschedule = { onRescheduleTask(taskItem.task.id) },
                                    onDelete = { onDeleteTask(taskItem.task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
