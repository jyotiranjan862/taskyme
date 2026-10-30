package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity> = emptyList()
)

data class TaskItem(
    val task: TaskEntity,
    val category: CategoryEntity?,
    val subtasks: List<SubtaskEntity> = emptyList()
) {
    val completedSubtaskCount: Int
        get() = subtasks.count { it.isCompleted }

    val totalSubtaskCount: Int
        get() = subtasks.size

    val isOverdue: Boolean
        get() {
            if (task.isCompleted || task.isArchived) return false
            val deadline = task.deadlineTimestamp ?: return false
            return System.currentTimeMillis() > deadline
        }
}
