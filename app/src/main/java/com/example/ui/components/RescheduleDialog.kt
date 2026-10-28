package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PriorityUrgent
import java.util.Calendar

@Composable
fun RescheduleDialog(
    taskId: Long,
    onDismiss: () -> Unit,
    onReschedule: (newDeadline: Long?, hasSpecificTime: Boolean) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reschedule Deadline",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preset 1: Later Today (+3 hours)
                RescheduleOptionItem(
                    icon = Icons.Default.Schedule,
                    title = "Later Today",
                    subtitle = "+3 hours from now",
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.HOUR_OF_DAY, 3)
                        onReschedule(cal.timeInMillis, true)
                        onDismiss()
                    }
                )

                // Preset 2: Tomorrow Morning (9:00 AM)
                RescheduleOptionItem(
                    icon = Icons.Default.WbSunny,
                    title = "Tomorrow Morning",
                    subtitle = "9:00 AM",
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        cal.set(Calendar.HOUR_OF_DAY, 9)
                        cal.set(Calendar.MINUTE, 0)
                        onReschedule(cal.timeInMillis, true)
                        onDismiss()
                    }
                )

                // Preset 3: Next Week (Monday 9:00 AM)
                RescheduleOptionItem(
                    icon = Icons.Default.DateRange,
                    title = "Next Week",
                    subtitle = "Monday 9:00 AM",
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.WEEK_OF_YEAR, 1)
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        cal.set(Calendar.HOUR_OF_DAY, 9)
                        cal.set(Calendar.MINUTE, 0)
                        onReschedule(cal.timeInMillis, true)
                        onDismiss()
                    }
                )

                // Preset 4: Custom Date & Time
                RescheduleOptionItem(
                    icon = Icons.Default.Alarm,
                    title = "Pick Custom Date & Time",
                    subtitle = "Select exact date & hour",
                    onClick = {
                        val c = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                TimePickerDialog(
                                    context,
                                    { _, h, min ->
                                        val cal = Calendar.getInstance()
                                        cal.set(y, m, d, h, min)
                                        onReschedule(cal.timeInMillis, true)
                                        onDismiss()
                                    },
                                    12, 0, false
                                ).show()
                            },
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )

                // Preset 5: Remove Deadline
                RescheduleOptionItem(
                    icon = Icons.Default.Today,
                    title = "No Deadline",
                    subtitle = "Clear deadline completely",
                    isDestructive = true,
                    onClick = {
                        onReschedule(null, false)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RescheduleOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDestructive) PriorityUrgent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) PriorityUrgent else IndigoPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDestructive) PriorityUrgent else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
