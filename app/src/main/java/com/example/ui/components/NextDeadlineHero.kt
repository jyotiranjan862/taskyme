package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PriorityUrgent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NextDeadlineHero(
    taskItem: TaskItem?,
    onComplete: (Long) -> Unit,
    onReschedule: (Long) -> Unit,
    onCardClick: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (taskItem == null) return

    val task = taskItem.task
    val deadline = task.deadlineTimestamp ?: return
    val now = System.currentTimeMillis()
    val isOverdue = now > deadline
    val diff = deadline - now

    val countdownText = if (isOverdue) {
        val overdueMins = (-diff / (60 * 1000)).toInt()
        if (overdueMins < 60) "Overdue ${overdueMins}m" else "Overdue ${overdueMins / 60}h"
    } else {
        val mins = (diff / (60 * 1000)).toInt()
        val hours = mins / 60
        val days = hours / 24
        when {
            mins < 60 -> "Due in $mins min"
            hours < 24 -> "Due in ${hours}h ${mins % 60}m"
            days == 1 -> "Due tomorrow"
            else -> "Due in $days days"
        }
    }

    val exactTimeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(deadline))
    val accentColor = if (isOverdue) PriorityUrgent else CyanAccent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCardClick(taskItem) }
            .testTag("next_deadline_hero_card"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isOverdue) PriorityUrgent.copy(alpha = 0.35f) else IndigoPrimary.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left content: dot indicator + title + subtle time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = countdownText.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = " • $exactTimeStr",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right actions: minimal compact buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onReschedule(task.id) },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("hero_reschedule_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Reschedule",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                FilledIconButton(
                    onClick = { onComplete(task.id) },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = accentColor.copy(alpha = 0.18f),
                        contentColor = accentColor
                    ),
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("hero_complete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
