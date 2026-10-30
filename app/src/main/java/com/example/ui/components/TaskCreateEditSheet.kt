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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintSecondary
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
        mutableStateOf(Priority.fromString(taskToEdit?.task?.priority ?: "HIGH"))
    }

    var deadlineTimestamp by remember {
        mutableStateOf(
            taskToEdit?.task?.deadlineTimestamp ?: run {
                val c = Calendar.getInstance()
                c.set(Calendar.HOUR_OF_DAY, 16)
                c.set(Calendar.MINUTE, 20)
                c.timeInMillis
            }
        )
    }
    var hasSpecificTime by remember { mutableStateOf(taskToEdit?.task?.hasSpecificTime ?: true) }
    var reminderEnabled by remember {
        mutableStateOf(taskToEdit?.task?.reminderMinutesBefore != null && taskToEdit.task.reminderMinutesBefore >= 0)
    }

    val subtaskTitles = remember {
        mutableStateListOf<String>().apply {
            if (taskToEdit != null) {
                addAll(taskToEdit.subtasks.map { it.title })
            }
        }
    }
    var newSubtaskText by remember { mutableStateOf("") }

    val isEditing = taskToEdit != null

    fun doSave() {
        if (title.isBlank()) return
        val reminderMinutes = if (reminderEnabled) 60 else -1

        if (isEditing && taskToEdit != null) {
            val updatedTask = taskToEdit.task.copy(
                title = title.trim(),
                description = description.trim(),
                categoryId = selectedCategoryId,
                priority = selectedPriority.name,
                deadlineTimestamp = deadlineTimestamp,
                hasSpecificTime = hasSpecificTime,
                reminderMinutesBefore = reminderMinutes,
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
                false,
                null,
                30,
                "",
                subtaskTitles.toList()
            )
        }
        onDismiss()
    }

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
            // Header matching Screen 3 mockup (Cancel | New task | Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }

                Text(
                    text = if (isEditing) "Edit task" else "New task",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = { doSave() },
                    enabled = title.isNotBlank()
                ) {
                    Text(
                        text = "Save",
                        color = if (title.isNotBlank()) IndigoPrimary else MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TITLE Section
            Text(
                text = "TITLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Task title...", fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // NOTES Section
            Text(
                text = "NOTES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Add notes or details...", fontSize = 14.sp) },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_description_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // CATEGORY Section
            Text(
                text = "CATEGORY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) IndigoPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable { selectedCategoryId = category.id }
                            .testTag("category_chip_${category.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { onAddNewCategoryRequested() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PRIORITY Section (matching Screen 3 segmented control)
            Text(
                text = "PRIORITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.URGENT).forEach { p ->
                    val isSelected = selectedPriority == p

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) IndigoPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPriority = p }
                            .testTag("priority_btn_${p.name}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 9.dp)
                        ) {
                            Text(
                                text = p.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DUE Section (Date box on left, Time box on right)
            Text(
                text = "DUE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date picker box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
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
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val dateText = deadlineTimestamp?.let {
                            val nowCal = Calendar.getInstance()
                            val deadCal = Calendar.getInstance().apply { timeInMillis = it }
                            if (nowCal.get(Calendar.DAY_OF_YEAR) == deadCal.get(Calendar.DAY_OF_YEAR)) {
                                "Today"
                            } else {
                                SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(it))
                            }
                        } ?: "Pick Date"
                        Text(dateText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Time picker box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val c = Calendar.getInstance()
                            deadlineTimestamp?.let { c.timeInMillis = it }
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    val updated = Calendar.getInstance()
                                    deadlineTimestamp?.let { updated.timeInMillis = it }
                                    updated.set(Calendar.HOUR_OF_DAY, h)
                                    updated.set(Calendar.MINUTE, min)
                                    deadlineTimestamp = updated.timeInMillis
                                    hasSpecificTime = true
                                },
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                false
                            ).show()
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val timeText = if (deadlineTimestamp != null && hasSpecificTime) {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(deadlineTimestamp!!))
                        } else {
                            "Set Time"
                        }
                        Text(timeText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // REMINDER ROW (matching Screen 3 mockup)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remind me 1 hour before",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IndigoPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtasks inline checklist
            Text(
                text = "SUBTASKS (${subtaskTitles.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            subtaskTitles.forEachIndexed { index, sub ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text("•", color = IndigoPrimary, modifier = Modifier.padding(horizontal = 6.dp))
                    Text(sub, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { subtaskTitles.removeAt(index) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
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
                    placeholder = { Text("Add a step...", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Full-Width Button matching Screen 3 mockup
            Button(
                onClick = { doSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("create_task_submit_btn"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isEditing) "Save changes" else "Create task",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
