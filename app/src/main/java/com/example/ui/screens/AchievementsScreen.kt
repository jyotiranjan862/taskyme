package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityUrgent

enum class BadgeCategory(val title: String) {
    ALL("All"),
    MILESTONES("Milestones"),
    STREAKS("Streaks"),
    SPEED_FOCUS("Speed & Focus"),
    SPECIAL("Special")
}

@Composable
fun AchievementsScreen(
    achievements: List<AchievementEntity>,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(BadgeCategory.ALL) }
    var selectedBadgeDetail by remember { mutableStateOf<AchievementEntity?>(null) }

    val unlockedCount = remember(achievements) { achievements.count { it.isUnlocked } }
    val totalCount = remember(achievements) { achievements.size.coerceAtLeast(1) }
    val progressFraction = (unlockedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)

    val (rankTitle, tier, nextRank) = when {
        unlockedCount >= 40 -> Triple("Grandmaster", "Tier 5", "Supreme Legend")
        unlockedCount >= 25 -> Triple("Master Sprinter", "Tier 4", "Grandmaster")
        unlockedCount >= 15 -> Triple("Senior Producer", "Tier 3", "Master Sprinter")
        unlockedCount >= 5 -> Triple("Fast Pacer", "Tier 2", "Senior Producer")
        else -> Triple("Focus Starter", "Tier 1", "Fast Pacer")
    }

    val filteredBadges = remember(achievements, selectedCategory) {
        when (selectedCategory) {
            BadgeCategory.ALL -> achievements
            BadgeCategory.MILESTONES -> achievements.filter { it.id.startsWith("task_") || it.id == "first_task" || it.id == "getting_started" || it.id == "monthly_achiever" }
            BadgeCategory.STREAKS -> achievements.filter { it.id.startsWith("streak_") || it.id == "consistent_3" || it.id == "week_streak" }
            BadgeCategory.SPEED_FOCUS -> achievements.filter {
                it.id.startsWith("deadline_") || it.id == "on_time_first" || it.id == "speed_demon" ||
                        it.id == "zero_overdue" || it.id == "early_bird" || it.id == "night_owl" ||
                        it.id == "morning_power" || it.id == "weekend_warrior" || it.id == "busy_bee" ||
                        it.id == "focus_champion" || it.id == "power_day"
            }
            BadgeCategory.SPECIAL -> achievements.filter {
                it.id.startsWith("subtask_") || it.id == "first_subtask" || it.id == "organized_mind" ||
                        it.id == "work_pro" || it.id == "study_master" || it.id == "health_champion" ||
                        it.id == "personal_growth" || it.id == "urgent_slayer" || it.id == "urgent_5" ||
                        it.id == "high_priority_10" || it.id == "balanced_priorities" || it.id == "inbox_zero"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 10.dp)
    ) {
        // Header matching Screen 2 mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IndigoPrimary.copy(alpha = 0.12f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$unlockedCount/$totalCount Unlocked",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Current Rank Banner Card matching Screen 2 mockup
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = IndigoPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Trophy/Medal icon in rounded container
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CURRENT RANK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = tier,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Text(
                            text = rankTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )

                        Text(
                            text = if (unlockedCount > 10) "Top 10% of productive users" else "Keep crushing tasks to rank up",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress to next rank
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress to $nextRank",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Category Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(BadgeCategory.entries) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat.title,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2-Column Grid of 50 Badge Cards matching Screen 2 mockup
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredBadges, key = { it.id }) { item ->
                GridBadgeCard(
                    achievement = item,
                    onClick = { selectedBadgeDetail = item }
                )
            }
        }
    }

    // Detail Dialog when a badge is clicked
    selectedBadgeDetail?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadgeDetail = null },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (color, icon) = getBadgeVisuals(badge.id, badge.iconName)
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (badge.isUnlocked) color else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (badge.isUnlocked) icon else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (badge.isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (badge.isUnlocked) "UNLOCKED!" else "IN PROGRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isUnlocked) MintSecondary else IndigoPrimary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar
                    val frac = (badge.currentProgress.toFloat() / badge.maxProgress.toFloat()).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${badge.currentProgress} / ${badge.maxProgress}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { frac },
                        color = if (badge.isUnlocked) MintSecondary else IndigoPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBadgeDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun GridBadgeCard(
    achievement: AchievementEntity,
    onClick: () -> Unit
) {
    val isUnlocked = achievement.isUnlocked
    val progress = achievement.currentProgress
    val max = achievement.maxProgress

    val (badgeColor, iconVector) = getBadgeVisuals(achievement.id, achievement.iconName)

    val subtitle = when {
        isUnlocked -> "Completed ($max/$max)"
        else -> "$progress/$max progress"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shield / emblem with mini status badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isUnlocked) badgeColor else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) iconVector else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Mini circular indicator badge
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isUnlocked) Color.White else MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isUnlocked) badgeColor else MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUnlocked) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(9.dp))

                // Title
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Progress subtitle
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = if (isUnlocked) MintSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isUnlocked) FontWeight.Medium else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            // Bottom colored accent strip matching Screen 2 mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        if (isUnlocked) badgeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
            )
        }
    }
}

private fun getBadgeVisuals(id: String, iconName: String): Pair<Color, ImageVector> {
    return when {
        id == "first_task" -> Pair(Color(0xFF3B82F6), Icons.Default.CheckCircle)
        id == "getting_started" -> Pair(Color(0xFF10B981), Icons.Default.RocketLaunch)
        id == "consistent_3" -> Pair(Color(0xFFA855F7), Icons.Default.LocalFireDepartment)
        id == "week_streak" -> Pair(Color(0xFFF97316), Icons.Default.Whatshot)
        id == "deadline_hero" -> Pair(Color(0xFFEF4444), Icons.Default.Timer)
        id == "focus_champion" -> Pair(Color(0xFF06B6D4), Icons.Default.Bolt)
        id.startsWith("streak_") -> Pair(Color(0xFFF97316), Icons.Default.LocalFireDepartment)
        id.startsWith("deadline_") -> Pair(Color(0xFFEF4444), Icons.Default.Schedule)
        id.startsWith("subtask_") -> Pair(Color(0xFF06B6D4), Icons.Default.Check)
        id.startsWith("urgent_") -> Pair(Color(0xFFE11D48), Icons.Default.Bolt)
        id == "early_bird" -> Pair(Color(0xFFF59E0B), Icons.Default.Star)
        id == "night_owl" -> Pair(Color(0xFF6366F1), Icons.Default.Star)
        id == "weekend_warrior" -> Pair(Color(0xFF10B981), Icons.Default.Shield)
        id == "power_day" -> Pair(Color(0xFF8B5CF6), Icons.Default.MilitaryTech)
        id == "work_pro" -> Pair(Color(0xFF4F46E5), Icons.Default.Flag)
        id == "study_master" -> Pair(Color(0xFFD97706), Icons.Default.Star)
        id == "health_champion" -> Pair(Color(0xFF10B981), Icons.Default.CheckCircle)
        id == "personal_growth" -> Pair(Color(0xFFEC4899), Icons.Default.Shield)
        id == "inbox_zero" -> Pair(Color(0xFF10B981), Icons.Default.CheckCircle)
        id.startsWith("task_") -> Pair(Color(0xFF6366F1), Icons.Default.EmojiEvents)
        else -> Pair(IndigoPrimary, Icons.Default.WorkspacePremium)
    }
}
