package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@Composable
fun AchievementsScreen(
    achievements: List<AchievementEntity>,
    modifier: Modifier = Modifier
) {
    val unlockedCount = remember(achievements) { achievements.count { it.isUnlocked } }
    val totalCount = remember(achievements) { achievements.size.coerceAtLeast(1) }
    val progressFraction = unlockedCount.toFloat() / totalCount.toFloat()

    val (rankTitle, tier, nextRank) = when {
        unlockedCount >= 8 -> Triple("Grandmaster", "Tier 4", "Supreme Master")
        unlockedCount >= 5 -> Triple("Master Sprinter", "Tier 3", "Grandmaster")
        unlockedCount >= 2 -> Triple("Fast Pacer", "Tier 2", "Master Sprinter")
        else -> Triple("Focus Starter", "Tier 1", "Fast Pacer")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 10.dp)
    ) {
        // Header matching Screen 2 mockup (Achievements  • 2/10 Unlocked)
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
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                        fontSize = 11.sp,
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
                            text = "Top 15% of productive users",
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

        Spacer(modifier = Modifier.height(16.dp))

        // All Badges Section Header (ALL BADGES  •  Milestones)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ALL BADGES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Milestones",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = IndigoPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2-Column Grid of Badge Cards matching Screen 2 mockup
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            items(achievements, key = { it.id }) { item ->
                GridBadgeCard(achievement = item)
            }
        }
    }
}

@Composable
private fun GridBadgeCard(achievement: AchievementEntity) {
    val isUnlocked = achievement.isUnlocked
    val progress = achievement.currentProgress
    val max = achievement.maxProgress

    // Assigning distinct colors and emblems matching Screen 2
    val (badgeColor, iconVector) = when (achievement.id) {
        "first_task" -> Pair(Color(0xFF3B82F6), Icons.Default.Check) // Blue shield with check
        "getting_started" -> Pair(Color(0xFF10B981), Icons.Default.Flag) // Green flag
        "consistent_3" -> Pair(Color(0xFFA855F7), Icons.Default.WorkspacePremium) // Purple medal
        "week_streak" -> Pair(Color(0xFFF97316), Icons.Default.WaterDrop) // Orange droplet
        "deadline_hero" -> Pair(Color(0xFFEF4444), Icons.Default.Timer) // Red timer
        "focus_champion" -> Pair(Color(0xFF06B6D4), Icons.Default.Bolt) // Cyan bolt
        "weekly_goal" -> Pair(Color(0xFFEAB308), Icons.Default.EmojiEvents) // Gold trophy
        else -> Pair(IndigoPrimary, Icons.Default.RocketLaunch)
    }

    val subtitle = when {
        isUnlocked -> "Completed ($max/$max)"
        achievement.id == "getting_started" -> "$progress/$max tasks done"
        achievement.id == "consistent_3" -> "$progress/$max streaks"
        achievement.id == "week_streak" -> "$progress/$max days streak"
        achievement.id == "deadline_hero" -> "$progress/$max on-time"
        achievement.id == "focus_champion" -> "$progress/$max deep tasks"
        else -> "$progress/$max progress"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shield / Hexagon badge emblem with mini status badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
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

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Progress subtitle (e.g. "Completed (1/1)", "2/5 tasks done")
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
