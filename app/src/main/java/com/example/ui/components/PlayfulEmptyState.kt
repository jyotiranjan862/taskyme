package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.TaskFilter
import kotlin.math.sin

/**
 * Centered, clean TaskFlow empty state container.
 * Features TaskFlow's own branded productivity companion model (no Duolingo likeness),
 * with all unnecessary suggested quests removed.
 */
@Composable
fun PlayfulEmptyState(
    currentFilter: TaskFilter,
    onCreateTaskClick: (() -> Unit)? = null,
    onQuickAddQuest: ((title: String, categoryName: String, priority: Priority) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 440.dp)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentFilter) {
            TaskFilter.TASKS -> {
                // TaskFlow's own productivity companion illustration
                TaskFlowBrandIllustration(
                    modifier = Modifier
                        .size(136.dp)
                        .testTag("taskflow_brand_illustration")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Ready to Focus",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "You're all caught up! Tap the '+' icon anytime to add your next task and keep your momentum going.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            TaskFilter.COMPLETED -> {
                TaskFlowTrophyIllustration(
                    modifier = Modifier
                        .size(125.dp)
                        .testTag("taskflow_trophy_illustration")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "No Completed Tasks Yet",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "When you check off your active tasks, they will appear here to celebrate your progress.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            TaskFilter.ARCHIVE -> {
                TaskFlowArchiveIllustration(
                    modifier = Modifier
                        .size(120.dp)
                        .testTag("taskflow_archive_illustration")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Archive is Empty",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Swiped or dismissed tasks are kept safely here. You can restore or delete them permanently.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

/**
 * TaskFlow's signature productivity model illustration in pure Compose Canvas.
 * A sleek, high-tech, friendly productivity companion featuring:
 * - Rounded geometric cyber-chassis in TaskFlow Indigo & Cyan
 * - Glowing curved curved visor with friendly smiling gaze
 * - Floating interactive task checklist hologram
 * - Orbiting productivity energy ring with smooth floating animation
 * - Sparkling ambient focus stars
 */
@Composable
fun TaskFlowBrandIllustration(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "taskflow_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val yShift = floatOffset

        // 1. Soft glowing elliptical shadow beneath the floating companion
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF4F46E5).copy(alpha = 0.25f),
                    Color(0xFF06B6D4).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.92f),
                radius = w * 0.38f
            ),
            topLeft = Offset(w * 0.16f, h * 0.86f),
            size = Size(w * 0.68f, h * 0.12f)
        )

        // 2. Ambient focus sparkles around model
        drawSparkle(
            center = Offset(w * 0.86f, h * 0.20f + yShift * 0.8f),
            radius = w * 0.07f,
            color = Color(0xFF06B6D4) // Cyan
        )
        drawSparkle(
            center = Offset(w * 0.14f, h * 0.32f - yShift * 0.6f),
            radius = w * 0.055f,
            color = Color(0xFFA855F7) // Violet
        )
        drawSparkle(
            center = Offset(w * 0.88f, h * 0.68f + yShift * 0.5f),
            radius = w * 0.045f,
            color = Color(0xFF6366F1) // Indigo light
        )

        // 3. Orbiting productivity halo / energy ring behind companion
        drawOval(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF06B6D4).copy(alpha = 0.7f),
                    Color(0xFF4F46E5).copy(alpha = 0.2f),
                    Color(0xFFA855F7).copy(alpha = 0.8f),
                    Color(0xFF06B6D4).copy(alpha = 0.7f)
                ),
                center = Offset(w * 0.5f, h * 0.46f + yShift)
            ),
            topLeft = Offset(w * 0.12f, h * 0.38f + yShift),
            size = Size(w * 0.76f, h * 0.24f),
            style = Stroke(width = w * 0.024f)
        )

        // 4. Companion Chassis: Modern rounded capsule body
        val chassisBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6366F1), // Bright Indigo
                Color(0xFF4F46E5), // TaskFlow Primary Indigo
                Color(0xFF3730A3)  // Deep Indigo
            ),
            startY = h * 0.18f + yShift,
            endY = h * 0.82f + yShift
        )
        drawRoundRect(
            brush = chassisBrush,
            topLeft = Offset(w * 0.26f, h * 0.20f + yShift),
            size = Size(w * 0.48f, h * 0.56f),
            cornerRadius = CornerRadius(w * 0.24f, w * 0.24f)
        )

        // 5. Glossy curved head visor (Glass display)
        val visorBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A), // Slate 900
                Color(0xFF1E293B)  // Slate 800
            ),
            startY = h * 0.28f + yShift,
            endY = h * 0.52f + yShift
        )
        drawRoundRect(
            brush = visorBrush,
            topLeft = Offset(w * 0.32f, h * 0.30f + yShift),
            size = Size(w * 0.36f, h * 0.22f),
            cornerRadius = CornerRadius(w * 0.11f, w * 0.11f)
        )

        // Visor cyan glow outline
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF06B6D4).copy(alpha = 0.8f), Color(0xFFA855F7).copy(alpha = 0.8f))
            ),
            topLeft = Offset(w * 0.32f, h * 0.30f + yShift),
            size = Size(w * 0.36f, h * 0.22f),
            cornerRadius = CornerRadius(w * 0.11f, w * 0.11f),
            style = Stroke(width = w * 0.015f)
        )

        // Friendly illuminated eyes on visor (Happy glowing arc eyes)
        val eyeY = h * 0.39f + yShift
        // Left eye arc
        drawArc(
            color = Color(0xFF06B6D4),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.38f, eyeY - w * 0.02f),
            size = Size(w * 0.075f, w * 0.06f),
            style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
        )
        // Right eye arc
        drawArc(
            color = Color(0xFF06B6D4),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.545f, eyeY - w * 0.02f),
            size = Size(w * 0.075f, w * 0.06f),
            style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
        )

        // Cheerful digital smile
        drawArc(
            color = Color(0xFF38BDF8),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.45f, eyeY + w * 0.045f),
            size = Size(w * 0.10f, w * 0.05f),
            style = Stroke(width = w * 0.016f, cap = StrokeCap.Round)
        )

        // 6. Floating Holographic Checklist / Flow Shield in foreground
        val sheetLeft = w * 0.48f
        val sheetTop = h * 0.50f + yShift * 0.8f
        val sheetWidth = w * 0.36f
        val sheetHeight = h * 0.32f

        // Holographic Glass Panel
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.96f), Color(0xFFF1F5F9))
            ),
            topLeft = Offset(sheetLeft, sheetTop),
            size = Size(sheetWidth, sheetHeight),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )
        // Card Indigo Border
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFF6366F1), Color(0xFF06B6D4)),
                start = Offset(sheetLeft, sheetTop),
                end = Offset(sheetLeft + sheetWidth, sheetTop + sheetHeight)
            ),
            topLeft = Offset(sheetLeft, sheetTop),
            size = Size(sheetWidth, sheetHeight),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
            style = Stroke(width = w * 0.018f)
        )

        // Holographic Checkmark Line 1
        val check1 = Path().apply {
            moveTo(sheetLeft + sheetWidth * 0.20f, sheetTop + sheetHeight * 0.32f)
            lineTo(sheetLeft + sheetWidth * 0.32f, sheetTop + sheetHeight * 0.44f)
            lineTo(sheetLeft + sheetWidth * 0.52f, sheetTop + sheetHeight * 0.24f)
        }
        drawPath(
            path = check1,
            color = Color(0xFF4F46E5),
            style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
        )
        // Checklist text bar 1
        drawRoundRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(sheetLeft + sheetWidth * 0.58f, sheetTop + sheetHeight * 0.31f),
            size = Size(sheetWidth * 0.30f, sheetHeight * 0.08f),
            cornerRadius = CornerRadius(w * 0.015f, w * 0.015f)
        )

        // Holographic Checkmark Line 2
        val check2 = Path().apply {
            moveTo(sheetLeft + sheetWidth * 0.20f, sheetTop + sheetHeight * 0.62f)
            lineTo(sheetLeft + sheetWidth * 0.32f, sheetTop + sheetHeight * 0.74f)
            lineTo(sheetLeft + sheetWidth * 0.52f, sheetTop + sheetHeight * 0.54f)
        }
        drawPath(
            path = check2,
            color = Color(0xFF06B6D4),
            style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
        )
        // Checklist text bar 2
        drawRoundRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(sheetLeft + sheetWidth * 0.58f, sheetTop + sheetHeight * 0.61f),
            size = Size(sheetWidth * 0.25f, sheetHeight * 0.08f),
            cornerRadius = CornerRadius(w * 0.015f, w * 0.015f)
        )

        // 7. Companion side sensor pads (Floating magnetic hands)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF06B6D4), Color(0xFF4F46E5)),
                center = Offset(w * 0.24f, h * 0.56f + yShift),
                radius = w * 0.065f
            ),
            radius = w * 0.055f,
            center = Offset(w * 0.24f, h * 0.56f + yShift)
        )
    }
}

/**
 * TaskFlow Trophy Illustration for the Completed tab.
 */
@Composable
fun TaskFlowTrophyIllustration(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Ground shadow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFC800).copy(alpha = 0.22f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.90f),
                radius = w * 0.40f
            ),
            topLeft = Offset(w * 0.15f, h * 0.85f),
            size = Size(w * 0.70f, h * 0.12f)
        )

        // Sparkles
        drawSparkle(Offset(w * 0.18f, h * 0.25f), w * 0.06f, Color(0xFFFFC800))
        drawSparkle(Offset(w * 0.82f, h * 0.28f), w * 0.07f, Color(0xFF06B6D4))

        // Trophy base pedestal (TaskFlow Indigo)
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF6366F1), Color(0xFF4338CA))
            ),
            topLeft = Offset(w * 0.32f, h * 0.75f),
            size = Size(w * 0.36f, h * 0.12f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        // Pedestal stem
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFE5A500), Color(0xFFFFD233), Color(0xFFE5A500))
            ),
            topLeft = Offset(w * 0.44f, h * 0.60f),
            size = Size(w * 0.12f, h * 0.16f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
        )

        // Cup body
        val cupPath = Path().apply {
            moveTo(w * 0.25f, h * 0.20f)
            lineTo(w * 0.75f, h * 0.20f)
            cubicTo(
                w * 0.75f, h * 0.58f,
                w * 0.65f, h * 0.62f,
                w * 0.50f, h * 0.62f
            )
            cubicTo(
                w * 0.35f, h * 0.62f,
                w * 0.25f, h * 0.58f,
                w * 0.25f, h * 0.20f
            )
            close()
        }
        drawPath(
            path = cupPath,
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFE5A500), Color(0xFFFFC800), Color(0xFFFFDF66), Color(0xFFE5A500))
            )
        )

        // Trophy handles (Left & Right)
        drawArc(
            color = Color(0xFFE5A500),
            startAngle = 100f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.12f, h * 0.26f),
            size = Size(w * 0.22f, h * 0.26f),
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFFE5A500),
            startAngle = 280f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.66f, h * 0.26f),
            size = Size(w * 0.22f, h * 0.26f),
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
        )

        // Checkmark badge on trophy cup
        val checkPath = Path().apply {
            moveTo(w * 0.40f, h * 0.38f)
            lineTo(w * 0.48f, h * 0.46f)
            lineTo(w * 0.62f, h * 0.30f)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
        )
    }
}

/**
 * TaskFlow Archive Safe Illustration for the Archive tab.
 */
@Composable
fun TaskFlowArchiveIllustration(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Ground shadow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6366F1).copy(alpha = 0.20f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.90f),
                radius = w * 0.42f
            ),
            topLeft = Offset(w * 0.15f, h * 0.85f),
            size = Size(w * 0.70f, h * 0.12f)
        )

        // Little floating sparkle
        drawSparkle(Offset(w * 0.82f, h * 0.26f), w * 0.055f, Color(0xFF06B6D4))

        // Vault body
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF6366F1), Color(0xFF4338CA))
            ),
            topLeft = Offset(w * 0.20f, h * 0.42f),
            size = Size(w * 0.60f, h * 0.42f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )

        // Vault lid / top
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF818CF8), Color(0xFF6366F1))
            ),
            topLeft = Offset(w * 0.16f, h * 0.26f),
            size = Size(w * 0.68f, h * 0.20f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        // Cyan accent lock dial in center
        drawRoundRect(
            color = Color(0xFF06B6D4),
            topLeft = Offset(w * 0.44f, h * 0.40f),
            size = Size(w * 0.12f, h * 0.14f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )
        // Keyhole
        drawCircle(
            color = Color(0xFF0F172A),
            radius = w * 0.02f,
            center = Offset(w * 0.50f, h * 0.46f)
        )
    }
}

// Backward compatibility alias for any existing reference
@Composable
fun DuolingoMascotIllustration(modifier: Modifier = Modifier) {
    TaskFlowBrandIllustration(modifier = modifier)
}

private fun DrawScope.drawSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        cubicTo(center.x, center.y, center.x, center.y, center.x + radius, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y + radius)
        cubicTo(center.x, center.y, center.x, center.y, center.x - radius, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path = path, color = color)
}
