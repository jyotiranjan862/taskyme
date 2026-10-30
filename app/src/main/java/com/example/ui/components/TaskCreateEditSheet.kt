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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.Priority
import com.example.data.model.SubtaskEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskItem
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.PriorityUrgent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Optimized & streamlined Add / Edit Task Modal Bottom Sheet.
 * Direct implementation of reference design with:
 * - Upper-case section labels: TITLE, NOTES, CATEGORY, PRIORITY, DUE, SUBTASKS
 * - Instant Quick-Date shortcut presets (Today, Tomorrow, Weekend, Next Week)
 * - Side-by-side Date & Time picker surfaces
 * - Segmented priority pills (Low, Medium, High, Urgent)
 * - Responsive 1-tap category chips with color dots
 * - Frictionless subtask addition on Enter / Done
 * - Full-width tactile 'Create task' bottom capsule button
 */
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
    val focusManager = LocalFocusManager.current

    var title by remember { mutableStateOf(taskToEdit?.task?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.task?.description ?: "") }
    var selectedCategoryId by remember {
        mutableLongStateOf(taskToEdit?.task?.categoryId ?: (categories.firstOrNull()?.id ?: 1L))
    }
    var selectedPriority by remember {
        mutableStateOf(Priority.fromString(taskToEdit?.task?.priority ?: "HIGH"))
    }

    var deadlineTimestamp by remember {
        mutableStateOf<Long?>(
            taskToEdit?.task?.deadlineTimestamp ?: run {
                val c = Calendar.getInstance()
                c.set(Calendar.HOUR_OF_DAY, 16)
                c.set(Calendar.MINUTE, 20)
                c.set(Calendar.SECOND, 0)
                c.set(Calendar.MILLISECOND, 0)
                c.timeInMillis
            }
        )
    }
    var hasSpecificTime by remember { mutableStateOf(taskToEdit?.task?.hasSpecificTime ?: true) }
    var reminderEnabled by remember {
        mutableStateOf(taskToEdit?.task?.reminderMinutesBefore != null && taskToEdit.task.reminderMinutesBefore >= 0)
    }
    var reminderMinutesSelected by remember {
        mutableIntStateOf(
            if (taskToEdit?.task?.reminderMinutesBefore != null && taskToEdit.task.reminderMinutesBefore > 0) {
                taskToEdit.task.reminderMinutesBefore
            } else 60
        )
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
        val reminderMinutes = if (reminderEnabled) reminderMinutesSelected else -1

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
                title.trim(),
                description.trim(),
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

    // Quick Date Helpers
    fun setQuickDate(daysFromNow: Int, defaultHour: Int = 17, defaultMinute: Int = 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, daysFromNow)
        // Keep existing time if set, otherwise set default
        val hour = if (deadlineTimestamp != null && hasSpecificTime) {
            Calendar.getInstance().apply { timeInMillis = deadlineTimestamp!! }.get(Calendar.HOUR_OF_DAY)
        } else defaultHour
        val minute = if (deadlineTimestamp != null && hasSpecificTime) {
            Calendar.getInstance().apply { timeInMillis = deadlineTimestamp!! }.get(Calendar.MINUTE)
        } else defaultMinute
        c.set(Calendar.HOUR_OF_DAY, hour)
        c.set(Calendar.MINUTE, minute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        deadlineTimestamp = c.timeInMillis
    }

    fun setQuickNextWeekend() {
        val c = Calendar.getInstance()
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        val daysUntilSaturday = (Calendar.SATURDAY - dayOfWeek + 7) % 7
        val daysToAdd = if (daysUntilSaturday == 0) 7 else daysUntilSaturday
        setQuickDate(daysToAdd, 10, 0)
    }

    fun setQuickNextMonday() {
        val c = Calendar.getInstance()
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        val daysUntilMonday = (Calendar.MONDAY - dayOfWeek + 7) % 7
        val daysToAdd = if (daysUntilMonday == 0) 7 else daysUntilMonday
        setQuickDate(daysToAdd, 9, 0)
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
                .padding(bottom = 36.dp)
        ) {
            // Header matching Screen 3 mockup (Cancel | New task | Save)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_task_btn")
                ) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = if (isEditing) "Edit task" else "New task",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.5.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = { doSave() },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.testTag("header_save_task_btn")
                ) {
                    Text(
                        text = "Save",
                        color = if (title.isNotBlank()) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TITLE Section
            Text(
                text = "TITLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "e.g., Finalize presentation...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    if (title.isNotEmpty()) {
                        IconButton(onClick = { title = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear title",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // NOTES Section
            Text(
                text = "NOTES",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = {
                    Text(
                        text = "Add notes, context, or links...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_description_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CATEGORY Section
            Text(
                text = "CATEGORY",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories, key = { it.id }) { category ->
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
                            if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.name,
                                fontSize = 12.5.sp,
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
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .clickable { onAddNewCategoryRequested() }
                            .testTag("add_new_category_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add category",
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PRIORITY Section (Segmented control matching mockup)
            Text(
                text = "PRIORITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.URGENT).forEach { p ->
                    val isSelected = selectedPriority == p
                    val dotColor = when (p) {
                        Priority.LOW -> PriorityLow
                        Priority.MEDIUM -> PriorityMedium
                        Priority.HIGH -> PriorityHigh
                        Priority.URGENT -> PriorityUrgent
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) IndigoPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPriority = p }
                            .testTag("priority_btn_${p.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = p.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DUE Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DUE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (deadlineTimestamp != null) {
                    Text(
                        text = "Clear deadline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clickable { deadlineTimestamp = null }
                            .padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Quick Date shortcut presets for fast creation
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                item {
                    QuickDateChip(
                        label = "Today",
                        isSelected = isToday(deadlineTimestamp),
                        onClick = { setQuickDate(0) }
                    )
                }
                item {
                    QuickDateChip(
                        label = "Tomorrow",
                        isSelected = isTomorrow(deadlineTimestamp),
                        onClick = { setQuickDate(1) }
                    )
                }
                item {
                    QuickDateChip(
                        label = "This Weekend",
                        isSelected = false,
                        onClick = { setQuickNextWeekend() }
                    )
                }
                item {
                    QuickDateChip(
                        label = "Next Week",
                        isSelected = false,
                        onClick = { setQuickNextMonday() }
                    )
                }
            }

            // Date & Time picker surfaces
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date picker box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1.1f)
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
                        .testTag("date_picker_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pick date",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val dateText = deadlineTimestamp?.let {
                            formatDateDisplay(it)
                        } ?: "Pick Date"
                        Text(
                            text = dateText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Time picker box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(0.9f)
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
                        .testTag("time_picker_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Pick time",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val timeText = if (deadlineTimestamp != null && hasSpecificTime) {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(deadlineTimestamp!!))
                        } else {
                            "Set Time"
                        }
                        Text(
                            text = timeText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                Column {
                    Text(
                        text = "Remind me before deadline",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (reminderEnabled) {
                        Text(
                            text = when (reminderMinutesSelected) {
                                15 -> "15 minutes before"
                                30 -> "30 minutes before"
                                60 -> "1 hour before"
                                1440 -> "1 day before"
                                else -> "$reminderMinutesSelected mins before"
                            },
                            fontSize = 11.sp,
                            color = IndigoPrimary
                        )
                    }
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IndigoPrimary
                    ),
                    modifier = Modifier.testTag("reminder_switch")
                )
            }

            // Quick reminder time pills if enabled
            if (reminderEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15 to "15m", 30 to "30m", 60 to "1h", 1440 to "1d").forEach { (mins, label) ->
                        val isSel = reminderMinutesSelected == mins
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) IndigoPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSel) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .clickable { reminderMinutesSelected = mins }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) IndigoPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtasks inline checklist
            Text(
                text = "SUBTASKS (${subtaskTitles.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            subtaskTitles.forEachIndexed { index, sub ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sub,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { subtaskTitles.removeAt(index) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove subtask",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSubtaskText,
                    onValueChange = { newSubtaskText = it },
                    placeholder = {
                        Text(
                            text = "Add subtask step...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newSubtaskText.isNotBlank()) {
                                subtaskTitles.add(newSubtaskText.trim())
                                newSubtaskText = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Full-Width Capsule Button matching Screen 3 mockup
            Button(
                onClick = { doSave() },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("create_task_submit_btn"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = IndigoPrimary.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 1.dp
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

@Composable
private fun QuickDateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) IndigoPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun isToday(timestamp: Long?): Boolean {
    if (timestamp == null) return false
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}

private fun isTomorrow(timestamp: Long?): Boolean {
    if (timestamp == null) return false
    val tom = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    return tom.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            tom.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}

private fun formatDateDisplay(timestamp: Long): String {
    val nowCal = Calendar.getInstance()
    val deadCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    return when {
        nowCal.get(Calendar.YEAR) == deadCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) == deadCal.get(Calendar.DAY_OF_YEAR) -> "Today"
        nowCal.get(Calendar.YEAR) == deadCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) + 1 == deadCal.get(Calendar.DAY_OF_YEAR) -> "Tomorrow"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
