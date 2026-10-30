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
            // 1. Task Completion Milestones (12 Badges)
            AchievementEntity(
                id = "first_task",
                title = "First Step",
                description = "Complete your very first task",
                iconName = "CheckCircle",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "task_3",
                title = "Triple Threat",
                description = "Complete 3 tasks successfully",
                iconName = "Check",
                maxProgress = 3
            ),
            AchievementEntity(
                id = "getting_started",
                title = "Getting Started",
                description = "Complete 5 tasks successfully",
                iconName = "RocketLaunch",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "task_10",
                title = "Tenacious Ten",
                description = "Complete 10 tasks across your days",
                iconName = "Flag",
                maxProgress = 10
            ),
            AchievementEntity(
                id = "task_20",
                title = "Momentum Builder",
                description = "Complete 20 tasks total",
                iconName = "Bolt",
                maxProgress = 20
            ),
            AchievementEntity(
                id = "task_35",
                title = "Flow State",
                description = "Complete 35 tasks with consistent action",
                iconName = "Timer",
                maxProgress = 35
            ),
            AchievementEntity(
                id = "task_50",
                title = "Half Century",
                description = "Reach 50 completed tasks milestone",
                iconName = "Shield",
                maxProgress = 50
            ),
            AchievementEntity(
                id = "task_75",
                title = "Productivity Pro",
                description = "Knock out 75 tasks across your projects",
                iconName = "MilitaryTech",
                maxProgress = 75
            ),
            AchievementEntity(
                id = "monthly_achiever",
                title = "Century Achiever",
                description = "Complete 100 tasks in total",
                iconName = "WorkspacePremium",
                maxProgress = 100
            ),
            AchievementEntity(
                id = "task_150",
                title = "Master Executor",
                description = "Complete 150 tasks total",
                iconName = "Star",
                maxProgress = 150
            ),
            AchievementEntity(
                id = "task_250",
                title = "Quarter Grand",
                description = "Reach 250 completed tasks",
                iconName = "EmojiEvents",
                maxProgress = 250
            ),
            AchievementEntity(
                id = "task_500",
                title = "Titan of Done",
                description = "Legendary status: complete 500 tasks",
                iconName = "EmojiEvents",
                maxProgress = 500
            ),

            // 2. Streaks & Consistency (10 Badges)
            AchievementEntity(
                id = "streak_2",
                title = "Back to Back",
                description = "Complete tasks 2 days in a row",
                iconName = "Timer",
                maxProgress = 2
            ),
            AchievementEntity(
                id = "consistent_3",
                title = "Consistent",
                description = "Maintain a 3-day task completion streak",
                iconName = "LocalFireDepartment",
                maxProgress = 3
            ),
            AchievementEntity(
                id = "streak_5",
                title = "High Five",
                description = "Maintain a 5-day task completion streak",
                iconName = "LocalFireDepartment",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "week_streak",
                title = "Streak Master",
                description = "Pure 1 week! Maintain a 7-day task streak",
                iconName = "Whatshot",
                maxProgress = 7
            ),
            AchievementEntity(
                id = "streak_10",
                title = "Double Digits",
                description = "Maintain a 10-day completion streak",
                iconName = "Whatshot",
                maxProgress = 10
            ),
            AchievementEntity(
                id = "streak_14",
                title = "Fortnight Flow",
                description = "Maintain an unbroken 14-day completion streak",
                iconName = "LocalFireDepartment",
                maxProgress = 14
            ),
            AchievementEntity(
                id = "streak_21",
                title = "Habit Formed",
                description = "Maintain a 21-day streak to lock in habits",
                iconName = "Shield",
                maxProgress = 21
            ),
            AchievementEntity(
                id = "streak_30",
                title = "Monthly Legend",
                description = "Maintain an unbroken 30-day streak",
                iconName = "WorkspacePremium",
                maxProgress = 30
            ),
            AchievementEntity(
                id = "streak_60",
                title = "Unstoppable",
                description = "Maintain an unbroken 60-day streak",
                iconName = "MilitaryTech",
                maxProgress = 60
            ),
            AchievementEntity(
                id = "streak_100",
                title = "Centurion Streak",
                description = "Maintain a monumental 100-day completion streak",
                iconName = "EmojiEvents",
                maxProgress = 100
            ),

            // 3. Punctuality & Speed (6 Badges)
            AchievementEntity(
                id = "on_time_first",
                title = "Punctual Start",
                description = "Complete your first task on or before deadline",
                iconName = "Schedule",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "deadline_hero",
                title = "Deadline Hero",
                description = "Complete 10 tasks on or before their deadline",
                iconName = "Timer",
                maxProgress = 10
            ),
            AchievementEntity(
                id = "deadline_25",
                title = "Clockwork Precision",
                description = "Complete 25 tasks on or before their deadline",
                iconName = "Schedule",
                maxProgress = 25
            ),
            AchievementEntity(
                id = "deadline_50",
                title = "Master of Time",
                description = "Complete 50 tasks on or before their deadline",
                iconName = "Timer",
                maxProgress = 50
            ),
            AchievementEntity(
                id = "speed_demon",
                title = "Ahead of Time",
                description = "Complete a task with more than 12 hours to spare",
                iconName = "Bolt",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "zero_overdue",
                title = "Zero Overdue",
                description = "Have 0 overdue tasks while completing at least 5 tasks",
                iconName = "CheckCircle",
                maxProgress = 5
            ),

            // 4. Daily Focus & Productivity Surges (6 Badges)
            AchievementEntity(
                id = "early_bird",
                title = "Early Bird",
                description = "Complete a task before 8:00 AM",
                iconName = "Star",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "night_owl",
                title = "Night Owl",
                description = "Complete a task after 10:00 PM",
                iconName = "Star",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "morning_power",
                title = "Morning Blitz",
                description = "Complete 3 tasks before noon",
                iconName = "Bolt",
                maxProgress = 3
            ),
            AchievementEntity(
                id = "weekend_warrior",
                title = "Weekend Warrior",
                description = "Complete tasks on a Saturday or Sunday",
                iconName = "Shield",
                maxProgress = 2
            ),
            AchievementEntity(
                id = "busy_bee",
                title = "Busy Bee",
                description = "Complete 3 tasks in a single day",
                iconName = "CheckCircle",
                maxProgress = 3
            ),
            AchievementEntity(
                id = "focus_champion",
                title = "Focus Champion",
                description = "Complete 5 tasks in a single day",
                iconName = "Bolt",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "power_day",
                title = "Powerhouse Day",
                description = "Complete 10 tasks in a single day",
                iconName = "MilitaryTech",
                maxProgress = 10
            ),

            // 5. Subtasks & Detailed Execution (5 Badges)
            AchievementEntity(
                id = "first_subtask",
                title = "Step by Step",
                description = "Complete your first subtask step",
                iconName = "Check",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "subtask_5",
                title = "Micro Planner",
                description = "Complete 5 subtask steps across tasks",
                iconName = "CheckCircle",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "subtask_slayer",
                title = "Detail Oriented",
                description = "Complete 15 subtasks across your projects",
                iconName = "CheckCircle",
                maxProgress = 15
            ),
            AchievementEntity(
                id = "subtask_30",
                title = "Checklist Champion",
                description = "Complete 30 subtasks across your tasks",
                iconName = "Shield",
                maxProgress = 30
            ),
            AchievementEntity(
                id = "subtask_50",
                title = "Executioner",
                description = "Complete 50 subtasks across your tasks",
                iconName = "WorkspacePremium",
                maxProgress = 50
            ),

            // 6. Categories & Life Balance (5 Badges)
            AchievementEntity(
                id = "organized_mind",
                title = "Organized Mind",
                description = "Create or complete tasks across at least 4 categories",
                iconName = "FolderSpecial",
                maxProgress = 4
            ),
            AchievementEntity(
                id = "work_pro",
                title = "Work Prodigy",
                description = "Complete 5 tasks categorized as Work",
                iconName = "Flag",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "study_master",
                title = "Scholar",
                description = "Complete 5 tasks categorized as Learning",
                iconName = "Star",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "health_champion",
                title = "Vitality",
                description = "Complete 5 tasks categorized as Health",
                iconName = "CheckCircle",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "personal_growth",
                title = "Life Balancer",
                description = "Complete 5 tasks categorized as Personal",
                iconName = "Shield",
                maxProgress = 5
            ),

            // 7. Priorities & Clean Slate (5 Badges)
            AchievementEntity(
                id = "urgent_slayer",
                title = "Crisis Defused",
                description = "Complete an Urgent priority task",
                iconName = "Bolt",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "urgent_5",
                title = "Firefighter",
                description = "Complete 5 Urgent priority tasks",
                iconName = "LocalFireDepartment",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "high_priority_10",
                title = "High Impact",
                description = "Complete 10 High priority tasks",
                iconName = "MilitaryTech",
                maxProgress = 10
            ),
            AchievementEntity(
                id = "balanced_priorities",
                title = "Master Prioritizer",
                description = "Complete tasks across Low, Medium, High & Urgent",
                iconName = "WorkspacePremium",
                maxProgress = 4
            ),
            AchievementEntity(
                id = "inbox_zero",
                title = "Clean Slate",
                description = "Complete all active tasks for the day",
                iconName = "CheckCircle",
                maxProgress = 1
            )
        )
    }
}
