package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.Priority
import com.example.data.model.SubtaskEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskItem
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.PriorityUrgent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskCreateEditSheet(
    taskToEdit: TaskItem?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSaveTask: (
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
    ) -> Unit,
    onUpdateExistingTask: (
        task: TaskEntity,
        subtasks: List<SubtaskEntity>
    ) -> Unit,
    onAddNewCategoryRequested: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var title by remember { mutableStateOf(taskToEdit?.task?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.task?.description ?: "") }
    var selectedCategoryId by remember {
        mutableLongStateOf(taskToEdit?.task?.categoryId ?: (categories.firstOrNull()?.id ?: 1L))
    }
    var selectedPriority by remember {
        mutableStateOf(Priority.fromString(taskToEdit?.task?.priority ?: "MEDIUM"))
    }

    var deadlineTimestamp by remember { mutableStateOf(taskToEdit?.task?.deadlineTimestamp) }
    var hasSpecificTime by remember { mutableStateOf(taskToEdit?.task?.hasSpecificTime ?: false) }
    var reminderMinutes by remember { mutableIntStateOf(taskToEdit?.task?.reminderMinutesBefore ?: 15) }
    var isPersistentNotification by remember {
        mutableStateOf(taskToEdit?.task?.isPersistentNotificationEnabled ?: false)
    }
    var recurringRule by remember { mutableStateOf(taskToEdit?.task?.recurringRule) }
    var estimatedDuration by remember { mutableIntStateOf(taskToEdit?.task?.estimatedDurationMinutes ?: 30) }
    var tags by remember { mutableStateOf(taskToEdit?.task?.tags ?: "") }

    val subtaskTitles = remember {
        mutableStateListOf<String>().apply {
            if (taskToEdit != null) {
                addAll(taskToEdit.subtasks.map { it.title })
            }
        }
    }
    var newSubtaskText by remember { mutableStateOf("") }

    val isEditing = taskToEdit != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Task" else "Create Task",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title *") },
                placeholder = { Text("e.g. Complete quarterly financial review") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details & Notes (optional)") },
                placeholder = { Text("Add context, links, or notes...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_description_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categories row
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategoryId == category.id
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (_: Exception) {
                        IndigoPrimary
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) catColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) BorderStroke(1.5.dp, catColor) else null,
                        modifier = Modifier
                            .clickable { selectedCategoryId = category.id }
                            .testTag("category_chip_${category.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onAddNewCategoryRequested,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Selector
            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Priority.entries.forEach { p ->
                    val isSelected = selectedPriority == p
                    val pColor = when (p) {
                        Priority.URGENT -> PriorityUrgent
                        Priority.HIGH -> PriorityHigh
                        Priority.MEDIUM -> PriorityMedium
                        Priority.LOW -> PriorityLow
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) pColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) BorderStroke(1.5.dp, pColor) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPriority = p }
                            .testTag("priority_btn_${p.name}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = p.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Deadline Section
            Text(
                text = "Deadline & Schedule",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Quick date presets
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val cal = Calendar.getInstance()

                // Preset: Today 5 PM
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        cal.timeInMillis = System.currentTimeMillis()
                        cal.set(Calendar.HOUR_OF_DAY, 17)
                        cal.set(Calendar.MINUTE, 0)
                        deadlineTimestamp = cal.timeInMillis
                        hasSpecificTime = true
                    }
                ) {
                    Text(
                        text = "Today 5:00 PM",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Preset: Tonight 9 PM
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        cal.timeInMillis = System.currentTimeMillis()
                        cal.set(Calendar.HOUR_OF_DAY, 21)
                        cal.set(Calendar.MINUTE, 0)
                        deadlineTimestamp = cal.timeInMillis
                        hasSpecificTime = true
                    }
                ) {
                    Text(
                        text = "Tonight 9:00 PM",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Preset: Tomorrow 10 AM
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        cal.timeInMillis = System.currentTimeMillis()
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        cal.set(Calendar.HOUR_OF_DAY, 10)
                        cal.set(Calendar.MINUTE, 0)
                        deadlineTimestamp = cal.timeInMillis
                        hasSpecificTime = true
                    }
                ) {
                    Text(
                        text = "Tomorrow 10:00 AM",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Preset: No Deadline
                if (deadlineTimestamp != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PriorityUrgent.copy(alpha = 0.12f),
                        modifier = Modifier.clickable {
                            deadlineTimestamp = null
                            hasSpecificTime = false
                        }
                    ) {
                        Text(
                            text = "Clear Deadline",
                            fontSize = 12.sp,
                            color = PriorityUrgent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date & Time pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pick Date
                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance()
                        deadlineTimestamp?.let { c.timeInMillis = it }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val updated = Calendar.getInstance()
                                deadlineTimestamp?.let { updated.timeInMillis = it }
                                updated.set(Calendar.YEAR, y)
                                updated.set(Calendar.MONTH, m)
                                updated.set(Calendar.DAY_OF_MONTH, d)
                                deadlineTimestamp = updated.timeInMillis
                            },
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    val dateStr = deadlineTimestamp?.let {
                        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(it))
                    } ?: "Pick Date"
                    Text(dateStr, fontSize = 12.sp)
                }

                // Pick Time
                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance()
                        deadlineTimestamp?.let { c.timeInMillis = it }
                        TimePickerDialog(
                            context,
                            { _, h, min ->
                                val updated = Calendar.getInstance()
                                if (deadlineTimestamp != null) {
                                    updated.timeInMillis = deadlineTimestamp!!
                                }
                                updated.set(Calendar.HOUR_OF_DAY, h)
                                updated.set(Calendar.MINUTE, min)
                                deadlineTimestamp = updated.timeInMillis
                                hasSpecificTime = true
                            },
                            c.get(Calendar.HOUR_OF_DAY),
                            c.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    val timeStr = if (deadlineTimestamp != null && hasSpecificTime) {
                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(deadlineTimestamp!!))
                    } else {
                        "Set Time"
                    }
                    Text(timeStr, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reminder Options
            Text(
                text = "Reminder Alarm",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            val reminderOptions = listOf(
                -1 to "None",
                0 to "At deadline",
                15 to "15m before",
                30 to "30m before",
                60 to "1h before",
                1440 to "1 day before"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(reminderOptions) { (mins, label) ->
                    val isSelected = reminderMinutes == mins
                    FilterChip(
                        selected = isSelected,
                        onClick = { reminderMinutes = mins },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                            selectedLabelColor = CyanAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Persistent Notification Switch
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Persistent Notification",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Text(
                            text = "Pins live countdown in Android notification shade",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isPersistentNotification,
                        onCheckedChange = { isPersistentNotification = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndigoPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtasks Builder
            Text(
                text = "Subtasks Checklist (${subtaskTitles.size})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            subtaskTitles.forEachIndexed { index, sub ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "•",
                        fontSize = 18.sp,
                        color = IndigoPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { subtaskTitles.removeAt(index) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove subtask", modifier = Modifier.size(14.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSubtaskText,
                    onValueChange = { newSubtaskText = it },
                    placeholder = { Text("Add subtask step...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newSubtaskText.isNotBlank()) {
                            subtaskTitles.add(newSubtaskText.trim())
                            newSubtaskText = ""
                        }
                    },
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recurring Rule
            Text(
                text = "Recurring Schedule",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            val recurringOptions = listOf(
                null to "Does not repeat",
                "DAILY" to "Daily",
                "WEEKDAYS" to "Mon-Fri",
                "WEEKLY" to "Weekly",
                "MONTHLY" to "Monthly"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(recurringOptions) { (rule, label) ->
                    val isSelected = recurringRule == rule
                    FilterChip(
                        selected = isSelected,
                        onClick = { recurringRule = rule },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = IndigoPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save / Submit Button
            Button(
                onClick = {
                    if (title.isBlank()) return@Button

                    if (isEditing && taskToEdit != null) {
                        val updatedTask = taskToEdit.task.copy(
                            title = title.trim(),
                            description = description.trim(),
                            categoryId = selectedCategoryId,
                            priority = selectedPriority.name,
                            deadlineTimestamp = deadlineTimestamp,
                            hasSpecificTime = hasSpecificTime,
                            reminderMinutesBefore = reminderMinutes,
                            isPersistentNotificationEnabled = isPersistentNotification,
                            recurringRule = recurringRule,
                            estimatedDurationMinutes = estimatedDuration,
                            tags = tags.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                        val updatedSubtasks = subtaskTitles.mapIndexed { idx, subTitle ->
                            val existing = taskToEdit.subtasks.getOrNull(idx)
                            SubtaskEntity(
                                id = existing?.id ?: 0L,
                                taskId = taskToEdit.task.id,
                                title = subTitle,
                                isCompleted = existing?.isCompleted ?: false,
                                position = idx
                            )
                        }
                        onUpdateExistingTask(updatedTask, updatedSubtasks)
                    } else {
                        onSaveTask(
                            title,
                            description,
                            selectedCategoryId,
                            selectedPriority,
                            deadlineTimestamp,
                            hasSpecificTime,
                            reminderMinutes,
                            isPersistentNotification,
                            recurringRule,
                            estimatedDuration,
                            tags,
                            subtaskTitles.toList()
                        )
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_task_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Create Task",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
