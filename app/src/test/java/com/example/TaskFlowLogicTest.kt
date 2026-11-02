package com.example

import com.example.data.model.CategoryEntity
import com.example.data.model.Priority
import com.example.data.model.TaskEntity
import com.example.data.model.TaskItem
import com.example.data.repository.AppColorTheme
import com.example.data.repository.AppThemeMode
import com.example.ui.analytics.AnalyticsCalculator
import com.example.ui.analytics.AnalyticsTimeframe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskFlowLogicTest {

    @Test
    fun `analytics calculation computes accurate metrics from completed tasks`() {
        val now = System.currentTimeMillis()
        val categories = CategoryEntity.defaultCategories()
        val workCategory = categories.first { it.name == "Work" }

        val task1 = TaskItem(
            task = TaskEntity(
                id = 1,
                title = "Finish deck",
                categoryId = workCategory.id,
                priority = Priority.URGENT.name,
                deadlineTimestamp = now + 3600000,
                isCompleted = true,
                completedAt = now + 1800000
            ),
            category = workCategory
        )

        val task2 = TaskItem(
            task = TaskEntity(
                id = 2,
                title = "Review PR",
                categoryId = workCategory.id,
                priority = Priority.HIGH.name,
                deadlineTimestamp = now - 3600000,
                isCompleted = false
            ),
            category = workCategory
        )

        val summary = AnalyticsCalculator.computeSummary(
            listOf(task1, task2),
            categories,
            AnalyticsTimeframe.LAST_7_DAYS
        )

        assertEquals(1, summary.totalCompleted)
        assertEquals(1, summary.totalPending)
        assertEquals(1, summary.totalOverdue)
        assertTrue(summary.onTimeCompletionRate > 0f)
    }

    @Test
    fun `task filter and archive state verify overdue calculation`() {
        val now = System.currentTimeMillis()
        val overdueActiveTask = TaskItem(
            task = TaskEntity(
                id = 10,
                title = "Late report",
                deadlineTimestamp = now - 60000,
                isCompleted = false,
                isArchived = false
            ),
            category = null
        )
        val overdueArchivedTask = TaskItem(
            task = TaskEntity(
                id = 11,
                title = "Old archived task",
                deadlineTimestamp = now - 60000,
                isCompleted = false,
                isArchived = true
            ),
            category = null
        )

        assertTrue(overdueActiveTask.isOverdue)
        assertFalse(overdueArchivedTask.isOverdue)
    }

    @Test
    fun `color themes provide valid hex color values`() {
        AppColorTheme.entries.forEach { theme ->
            assertTrue(theme.title.isNotBlank())
            assertTrue(theme.primaryHex != 0L)
            assertTrue(theme.primaryDarkHex != 0L)
            assertTrue(theme.secondaryHex != 0L)
        }
    }
}
