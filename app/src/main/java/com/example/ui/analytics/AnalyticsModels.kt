package com.example.ui.analytics

data class DayCompletionData(
    val dayLabel: String,      // e.g. "Mon", "Tue" or "Oct 24"
    val timestamp: Long,
    val completedCount: Int,
    val isToday: Boolean = false
)

data class CategoryShareData(
    val categoryName: String,
    val colorHex: String,
    val completedCount: Int,
    val totalCount: Int,
    val percentage: Float
)

data class AnalyticsPeriodSummary(
    val totalCreated: Int,
    val totalCompleted: Int,
    val totalPending: Int,
    val totalOverdue: Int,
    val completionRate: Float,        // 0.0 to 100.0
    val onTimeCompletionRate: Float,  // 0.0 to 100.0
    val currentStreak: Int,
    val longestStreak: Int,
    val mostProductiveDay: String,
    val periodCompletionChangePercent: Float, // comparison with prior period e.g. +14.2%
    val dailyCompletions: List<DayCompletionData>,
    val categoryShares: List<CategoryShareData>
)

enum class AnalyticsTimeframe(val label: String, val days: Int) {
    LAST_7_DAYS("7 Days", 7),
    LAST_30_DAYS("30 Days", 30),
    THIS_MONTH("This Month", 31),
    ALL_TIME("All Time", 365)
}
