package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1
) {
    companion object {
        fun defaultAchievements(): List<AchievementEntity> = listOf(
            AchievementEntity(
                id = "first_task",
                title = "First Step",
                description = "Complete your very first task",
                iconName = "CheckCircle",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "getting_started",
                title = "Getting Started",
                description = "Complete 5 tasks successfully",
                iconName = "RocketLaunch",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "consistent_3",
                title = "Consistent",
                description = "Maintain a 3-day task completion streak",
                iconName = "LocalFireDepartment",
                maxProgress = 3
            ),
            AchievementEntity(
                id = "week_streak",
                title = "Streak Master",
                description = "Maintain a 7-day task completion streak",
                iconName = "Whatshot",
                maxProgress = 7
            ),
            AchievementEntity(
                id = "deadline_hero",
                title = "Deadline Hero",
                description = "Complete 10 tasks on or before their deadline",
                iconName = "Timer",
                maxProgress = 10
            ),
            AchievementEntity(
                id = "focus_champion",
                title = "Focus Champion",
                description = "Complete 10 tasks in a single day",
                iconName = "Bolt",
                maxProgress = 10
            ),
            AchievementEntity(
                id = "weekly_goal",
                title = "Weekly Goal",
                description = "Hit your weekly task completion target",
                iconName = "EmojiEvents",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "monthly_achiever",
                title = "Century Achiever",
                description = "Complete 100 tasks total",
                iconName = "WorkspacePremium",
                maxProgress = 100
            ),
            AchievementEntity(
                id = "subtask_slayer",
                title = "Detail Oriented",
                description = "Complete 15 subtasks across your projects",
                iconName = "Checklist",
                maxProgress = 15
            ),
            AchievementEntity(
                id = "organized_mind",
                title = "Organized Mind",
                description = "Create tasks across at least 4 different categories",
                iconName = "FolderSpecial",
                maxProgress = 4
            )
        )
    }
}
