package com.example.ui.analytics

import com.example.data.model.CategoryEntity
import com.example.data.model.TaskItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AnalyticsCalculator {

    fun computeSummary(
        tasks: List<TaskItem>,
        categories: List<CategoryEntity>,
        timeframe: AnalyticsTimeframe
    ): AnalyticsPeriodSummary {
        val now = System.currentTimeMillis()
        val daysCount = timeframe.days

        val currentStartCal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -(daysCount - 1))
        }
        val currentPeriodStart = currentStartCal.timeInMillis

        val previousPeriodStart = Calendar.getInstance().apply {
            timeInMillis = currentPeriodStart
            add(Calendar.DAY_OF_YEAR, -daysCount)
        }.timeInMillis

        // Period tasks
        val currentPeriodTasks = tasks.filter {
            val timestamp = it.task.completedAt ?: it.task.createdAt
            timestamp >= currentPeriodStart
        }

        val previousPeriodTasks = tasks.filter {
            val timestamp = it.task.completedAt ?: it.task.createdAt
            timestamp in previousPeriodStart until currentPeriodStart
        }

        val totalCreated = tasks.count { it.task.createdAt >= currentPeriodStart }
        val completedInPeriod = currentPeriodTasks.filter { it.task.isCompleted }
        val totalCompleted = completedInPeriod.size
        val totalPending = tasks.count { !it.task.isCompleted }
        val totalOverdue = tasks.count { it.isOverdue }

        val completionRate = if (totalCreated + totalCompleted > 0) {
            (totalCompleted.toFloat() / (totalCreated + totalCompleted).coerceAtLeast(1)) * 100f
        } else if (tasks.isNotEmpty()) {
            (tasks.count { it.task.isCompleted }.toFloat() / tasks.size) * 100f
        } else {
            0f
        }

        // On-time completion: completed tasks where deadline is present and completedAt <= deadlineTimestamp
        val completedWithDeadline = completedInPeriod.filter { it.task.deadlineTimestamp != null }
        val onTimeCount = completedWithDeadline.count {
            val deadline = it.task.deadlineTimestamp ?: 0L
            val completed = it.task.completedAt ?: 0L
            completed <= deadline
        }
        val onTimeRate = if (completedWithDeadline.isNotEmpty()) {
            (onTimeCount.toFloat() / completedWithDeadline.size) * 100f
        } else {
            100f
        }

        // Comparison with previous period
        val prevCompletedCount = previousPeriodTasks.count { it.task.isCompleted }
        val percentChange = if (prevCompletedCount > 0) {
            ((totalCompleted - prevCompletedCount).toFloat() / prevCompletedCount) * 100f
        } else if (totalCompleted > 0) {
            100f
        } else {
            0f
        }

        // Streaks
        val allCompletedTasks = tasks.filter { it.task.isCompleted }
        val (curStreak, longStreak) = calculateStreaks(allCompletedTasks)

        // Most productive day of week
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayOfWeekCounts = mutableMapOf<String, Int>()
        completedInPeriod.forEach {
            val date = Date(it.task.completedAt ?: it.task.updatedAt)
            val dayName = dayOfWeekFormat.format(date)
            dayOfWeekCounts[dayName] = (dayOfWeekCounts[dayName] ?: 0) + 1
        }
        val mostProductiveDay = dayOfWeekCounts.maxByOrNull { it.value }?.key ?: "N/A"

        // Daily completions for chart
        val dailyCompletions = generateDailyCompletions(completedInPeriod, daysCount, now)

        // Category breakdown
        val categoryShares = generateCategoryShares(tasks, categories, currentPeriodStart)

        return AnalyticsPeriodSummary(
            totalCreated = totalCreated,
            totalCompleted = totalCompleted,
            totalPending = totalPending,
            totalOverdue = totalOverdue,
            completionRate = completionRate.coerceIn(0f, 100f),
            onTimeCompletionRate = onTimeRate.coerceIn(0f, 100f),
            currentStreak = curStreak,
            longestStreak = longStreak,
            mostProductiveDay = mostProductiveDay,
            periodCompletionChangePercent = percentChange,
            dailyCompletions = dailyCompletions,
            categoryShares = categoryShares
        )
    }

    private fun generateDailyCompletions(
        completedTasks: List<TaskItem>,
        daysCount: Int,
        now: Long
    ): List<DayCompletionData> {
        val result = mutableListOf<DayCompletionData>()
        val shortDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        val countsByDate = completedTasks.groupBy {
            dateKeyFormat.format(Date(it.task.completedAt ?: it.task.updatedAt))
        }.mapValues { it.value.size }

        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayKey = dateKeyFormat.format(cal.time)

        // Show max 14 points if daysCount > 14 so it fits cleanly on phone screen
        val points = daysCount.coerceAtMost(14)
        cal.add(Calendar.DAY_OF_YEAR, -(points - 1))

        for (i in 0 until points) {
            val key = dateKeyFormat.format(cal.time)
            val label = shortDayFormat.format(cal.time)
            val count = countsByDate[key] ?: 0
            val isToday = key == todayKey

            result.add(
                DayCompletionData(
                    dayLabel = label,
                    timestamp = cal.timeInMillis,
                    completedCount = count,
                    isToday = isToday
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    private fun generateCategoryShares(
        tasks: List<TaskItem>,
        categories: List<CategoryEntity>,
        periodStart: Long
    ): List<CategoryShareData> {
        val categoryMap = categories.associateBy { it.id }
        val categoryCounts = mutableMapOf<Long, Pair<Int, Int>>() // completed, total

        tasks.forEach { item ->
            val catId = item.task.categoryId
            val current = categoryCounts[catId] ?: Pair(0, 0)
            val newCompleted = if (item.task.isCompleted) current.first + 1 else current.first
            val newTotal = current.second + 1
            categoryCounts[catId] = Pair(newCompleted, newTotal)
        }

        val totalAll = categoryCounts.values.sumOf { it.second }.coerceAtLeast(1)

        return categoryCounts.mapNotNull { (catId, pair) ->
            val cat = categoryMap[catId]
            if (cat != null) {
                CategoryShareData(
                    categoryName = cat.name,
                    colorHex = cat.colorHex,
                    completedCount = pair.first,
                    totalCount = pair.second,
                    percentage = (pair.second.toFloat() / totalAll) * 100f
                )
            } else null
        }.sortedByDescending { it.totalCount }
    }

    private fun calculateStreaks(completedTasks: List<TaskItem>): Pair<Int, Int> {
        if (completedTasks.isEmpty()) return Pair(0, 0)

        val dateKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dayKeys = completedTasks.mapNotNull { it.task.completedAt }.map {
            dateKeyFormat.format(Date(it))
        }.distinct().sortedDescending()

        if (dayKeys.isEmpty()) return Pair(0, 0)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayKey = dateKeyFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = dateKeyFormat.format(cal.time)

        var curStreak = 0
        val daySet = dayKeys.toSet()

        var checkCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Current streak
        var checkKey = dateKeyFormat.format(checkCal.time)
        if (!daySet.contains(checkKey)) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            checkKey = dateKeyFormat.format(checkCal.time)
        }

        while (daySet.contains(checkKey)) {
            curStreak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            checkKey = dateKeyFormat.format(checkCal.time)
        }

        // Longest streak across all recorded days
        var longestStreak = curStreak
        var tempStreak = 0
        val sortedAscending = dayKeys.sorted()

        val iterCal = Calendar.getInstance()
        var prevTime: Long? = null

        sortedAscending.forEach { key ->
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(key)
            if (date != null) {
                if (prevTime == null) {
                    tempStreak = 1
                } else {
                    val diffDays = (date.time - prevTime!!) / (24 * 60 * 60 * 1000)
                    if (diffDays == 1L) {
                        tempStreak++
                    } else if (diffDays > 1L) {
                        tempStreak = 1
                    }
                }
                if (tempStreak > longestStreak) longestStreak = tempStreak
                prevTime = date.time
            }
        }

        return Pair(curStreak, longestStreak.coerceAtLeast(curStreak))
    }
}
