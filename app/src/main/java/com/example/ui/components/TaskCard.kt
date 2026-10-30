package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.ui.theme.CyanAccent
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    taskItem: TaskItem,
    onToggleCompletion: (Boolean) -> Unit,
    onToggleSubtask: (subtaskId: Long, isCompleted: Boolean) -> Unit,
    onEdit: () -> Unit,
    onReschedule: () -> Unit,
    onDelete: () -> Unit,
    onRestore: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val task = taskItem.task
    val isCompleted = task.isCompleted
    val isArchived = task.isArchived
    val isOverdue = taskItem.isOverdue
    var showMenu by remember { mutableStateOf(false) }
    var expandedSubtasks by remember { mutableStateOf(false) }

    val priority = Priority.fromString(task.priority)
    val priorityColor = when (priority) {
        Priority.URGENT -> PriorityUrgent
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW -> PriorityLow
    }

    val categoryColor = remember(taskItem.category?.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(taskItem.category?.colorHex ?: "#4F46E5"))
        } catch (_: Exception) {
            IndigoPrimary
        }
    }

    val deadlineText = remember(task.deadlineTimestamp, task.hasSpecificTime, isCompleted) {
        formatDeadlineLabel(task.deadlineTimestamp, task.hasSpecificTime, isCompleted)
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (isArchived) {
                        onRestore?.invoke()
                    } else {
                        onToggleCompletion(!isCompleted)
                    }
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("task_swipe_box_${task.id}"),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val bgColor = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> if (isArchived || isCompleted) PriorityUrgent else Color(0xFF64748B)
                SwipeToDismissBoxValue.StartToEnd -> when {
                    isArchived -> MintSecondary
                    isCompleted -> IndigoPrimary
                    else -> MintSecondary
                }
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> if (isArchived || isCompleted) Icons.Default.DeleteForever else Icons.Default.Archive
                SwipeToDismissBoxValue.StartToEnd -> when {
                    isArchived -> Icons.Default.Unarchive
                    isCompleted -> Icons.Default.Close
                    else -> Icons.Default.Check
                }
                else -> null
            }
            val label = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> if (isArchived || isCompleted) "Delete Forever" else "Archive"
                SwipeToDismissBoxValue.StartToEnd -> when {
                    isArchived -> "Restore"
                    isCompleted -> "Reopen"
                    else -> "Done"
                }
                else -> ""
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (icon != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .testTag("task_card_${task.id}"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                if (taskItem.isOverdue && !isCompleted) PriorityUrgent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            ),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Checkbox matching reference (Green filled circle when done, subtle outline when open)
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) MintSecondary else Color.Transparent
                            )
                            .clickable { onToggleCompletion(!isCompleted) }
                            .testTag("task_checkbox_${task.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.size(22.dp)
                            ) {}
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Top row: Category tag on left, Priority badge on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category: colored dot + label
                            taskItem.category?.let { cat ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(categoryColor)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = cat.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } ?: Spacer(modifier = Modifier.width(1.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Priority pill badge (matching reference: subtle tinted badge)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = priorityColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = priority.title,
                                        color = priorityColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }

                                // Quick options menu
                                Box {
                                    IconButton(
                                        onClick = { showMenu = true },
                                        modifier = Modifier
                                            .size(22.dp)
                                            .padding(start = 4.dp)
                                            .testTag("task_menu_btn_${task.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        if (isArchived) {
                                            DropdownMenuItem(
                                                text = { Text("Restore to Tasks") },
                                                leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    showMenu = false
                                                    onRestore?.invoke()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete Permanently", color = PriorityUrgent) },
                                                leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = PriorityUrgent, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    showMenu = false
                                                    onDelete()
                                                }
                                            )
                                        } else if (isCompleted) {
                                            DropdownMenuItem(
                                                text = { Text("Mark as Incomplete") },
                                                leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    showMenu = false
                                                    onToggleCompletion(false)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete Permanently", color = PriorityUrgent) },
                                                leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = PriorityUrgent, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    showMenu = false
                                                    onDelete()
                                                }
                                            )
                                        } else {
                                            DropdownMenuItem(
                                                text = { Text("Edit Task") },
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    showMenu = false
                                                    onEdit()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Reschedule") },
                                                leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    showMenu = false
                                                    onReschedule()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Move to Archive") },
                                                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                onClick = {
                                                    showMenu = false
                                                    onDelete()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Task Title
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (isCompleted || isArchived) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Notes / Description
                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom row: Time badge on left, Subtasks count on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Time badge pill
                            if (isArchived) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Archive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Archived",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else if (isCompleted) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MintSecondary.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MintSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Completed on time",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MintSecondary
                                        )
                                    }
                                }
                            } else if (deadlineText.isNotBlank()) {
                                val badgeBg = if (isOverdue) PriorityUrgent.copy(alpha = 0.14f) else Color(0xFFFEF3C7) // warm amber pill from reference
                                val badgeText = if (isOverdue) PriorityUrgent else Color(0xFFD97706)

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (MaterialTheme.colorScheme.background == Color(0xFF0B0F19)) badgeText.copy(alpha = 0.2f) else badgeBg
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = badgeText,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isOverdue) "LATE • $deadlineText" else deadlineText,
                                            fontSize = 11.sp,
                                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.SemiBold,
                                            color = badgeText
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            // Right: Subtasks count
                            if (taskItem.subtasks.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { expandedSubtasks = !expandedSubtasks }
                                ) {
                                    Text(
                                        text = "${taskItem.completedSubtaskCount}/${taskItem.totalSubtaskCount} subtasks",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = if (expandedSubtasks) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Expandable subtasks checklist
                AnimatedVisibility(visible = expandedSubtasks && taskItem.subtasks.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 36.dp, top = 8.dp)
                    ) {
                        taskItem.subtasks.forEach { subtask ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSubtask(subtask.id, !subtask.isCompleted) }
                                    .padding(vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = if (subtask.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    tint = if (subtask.isCompleted) MintSecondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (subtask.isCompleted) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDeadlineLabel(deadline: Long?, hasSpecificTime: Boolean, isCompleted: Boolean): String {
    if (deadline == null) return ""
    if (isCompleted) return "Completed"

    val now = System.currentTimeMillis()
    val diff = deadline - now

    val deadlineCal = Calendar.getInstance().apply { timeInMillis = deadline }
    val nowCal = Calendar.getInstance().apply { timeInMillis = now }

    val isSameDay = deadlineCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            deadlineCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    nowCal.add(Calendar.DAY_OF_YEAR, 1)
    val isTomorrow = deadlineCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            deadlineCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    if (diff < 0) {
        val overdueMins = (-diff / (60 * 1000)).toInt()
        return if (overdueMins < 60) "Overdue by ${overdueMins}m" else "Overdue by ${overdueMins / 60}h"
    }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = if (hasSpecificTime) timeFormat.format(Date(deadline)) else ""

    val remainingHours = (diff / (3600 * 1000)).toInt()
    val inHoursPart = if (remainingHours in 1..48) " (in ${remainingHours}h)" else ""

    return when {
        isSameDay -> if (hasSpecificTime) "$formattedTime$inHoursPart" else "Today"
        isTomorrow -> if (hasSpecificTime) "Tomorrow at $formattedTime" else "Tomorrow"
        else -> SimpleDateFormat("EEE, h:mm a", Locale.getDefault()).format(Date(deadline)) + inHoursPart
    }
}
