package com.example.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.analytics.CategoryShareData
import com.example.ui.analytics.DayCompletionData
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintSecondary

@Composable
fun DailyCompletionsBarChart(
    data: List<DayCompletionData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No activity recorded in this period", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxVal = remember(data) {
        val max = data.maxOfOrNull { it.completedCount } ?: 1
        if (max == 0) 4 else (max + 1)
    }

    val textPaintColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val todayBarColor = CyanAccent
    val normalBarColor = IndigoPrimary
    val barBaseColor = MaterialTheme.colorScheme.surfaceVariant

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(600))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 10.dp, bottom = 4.dp)
    ) {
        val width = size.width
        val height = size.height
        val bottomLabelSpace = 24.dp.toPx()
        val chartHeight = height - bottomLabelSpace
        val barCount = data.size
        val totalSpacing = width * 0.25f
        val barWidth = (width - totalSpacing) / barCount.coerceAtLeast(1)
        val spaceBetween = totalSpacing / (barCount + 1).coerceAtLeast(1)

        val paint = android.graphics.Paint().apply {
            color = textPaintColor
            textSize = 10.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        // Draw background horizontal reference lines
        val lineY1 = chartHeight * 0.25f
        val lineY2 = chartHeight * 0.75f
        drawLine(
            color = Color.Gray.copy(alpha = 0.15f),
            start = Offset(0f, lineY1),
            end = Offset(width, lineY1),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.15f),
            start = Offset(0f, lineY2),
            end = Offset(width, lineY2),
            strokeWidth = 1.dp.toPx()
        )

        data.forEachIndexed { index, item ->
            val left = spaceBetween + index * (barWidth + spaceBetween)
            val barHeightFraction = (item.completedCount.toFloat() / maxVal.toFloat()) * animationProgress.value
            val barH = chartHeight * barHeightFraction
            val top = chartHeight - barH

            // Bar background slot
            drawRoundRect(
                color = barBaseColor.copy(alpha = 0.4f),
                topLeft = Offset(left, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )

            // Animated completed value bar
            if (barH > 0f) {
                val brush = if (item.isToday) {
                    Brush.verticalGradient(
                        colors = listOf(CyanAccent, IndigoPrimary),
                        startY = top,
                        endY = chartHeight
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(normalBarColor, normalBarColor.copy(alpha = 0.7f)),
                        startY = top,
                        endY = chartHeight
                    )
                }

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )

                // Value label on top of bar if count > 0
                if (item.completedCount > 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        item.completedCount.toString(),
                        left + barWidth / 2,
                        (top - 4.dp.toPx()).coerceAtLeast(10.dp.toPx()),
                        paint
                    )
                }
            }

            // Day label below bar
            drawContext.canvas.nativeCanvas.drawText(
                item.dayLabel,
                left + barWidth / 2,
                height - 4.dp.toPx(),
                paint
            )
        }
    }
}

@Composable
fun CompletionRateRing(
    percentage: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(percentage) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(percentage / 100f, animationSpec = tween(800))
    }

    val primaryColor = IndigoPrimary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(90.dp)) {
                val strokeWidth = 8.dp.toPx()
                // Track
                drawArc(
                    color = trackColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Progress
                drawArc(
                    brush = Brush.linearGradient(listOf(CyanAccent, primaryColor)),
                    startAngle = 135f,
                    sweepAngle = 270f * animatedProgress.value.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryBreakdownBar(
    categories: List<CategoryShareData>,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Stacked Progress Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            categories.forEach { cat ->
                val color = try {
                    Color(android.graphics.Color.parseColor(cat.colorHex))
                } catch (_: Exception) {
                    IndigoPrimary
                }
                Box(
                    modifier = Modifier
                        .weight(cat.percentage.coerceAtLeast(1f))
                        .height(10.dp)
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend list
        categories.forEach { cat ->
            val color = try {
                Color(android.graphics.Color.parseColor(cat.colorHex))
            } catch (_: Exception) {
                IndigoPrimary
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cat.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${cat.completedCount}/${cat.totalCount} (${cat.percentage.toInt()}%)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
