package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Priority(val title: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4);

    companion object {
        fun fromString(value: String): Priority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class RecurringRule(val label: String) {
    NONE("Does not repeat"),
    DAILY("Daily"),
    WEEKDAYS("Weekdays (Mon-Fri)"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    companion object {
        fun fromString(value: String?): RecurringRule {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NONE
        }
    }
}

@Entity(
    tableName = "tasks",
    indices = [
        Index("deadlineTimestamp"),
        Index("categoryId"),
        Index("isCompleted"),
        Index("isArchived")
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: Long = 1,
    val priority: String = Priority.MEDIUM.name,
    val deadlineTimestamp: Long? = null,
    val hasSpecificTime: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val reminderMinutesBefore: Int = -1, // -1 means none
    val isPersistentNotificationEnabled: Boolean = false,
    val recurringRule: String? = null,
    val estimatedDurationMinutes: Int = 0,
    val tags: String = ""
)
