package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CategoryEntity
import com.example.data.model.Priority
import com.example.data.model.TaskEntity
import com.example.data.model.TaskItem
import com.example.ui.analytics.AnalyticsCalculator
import com.example.ui.analytics.AnalyticsTimeframe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies TaskFlow app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TaskFlow", appName)
    }

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
        org.junit.Assert.assertFalse(overdueArchivedTask.isOverdue)
    }
}
