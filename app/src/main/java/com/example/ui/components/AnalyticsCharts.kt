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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
fun TasksCompletedLineChart(
    data: List<DayCompletionData>,
    peakDayLabel: String,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No activity recorded yet", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val maxVal = remember(data) {
        val max = data.maxOfOrNull { it.completedCount } ?: 1
        if (max == 0) 2 else max
    }

    val textPaintColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val lineColor = IndigoPrimary
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(top = 18.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
    ) {
        val w = size.width
        val h = size.height
        val bottomSpace = 22.dp.toPx()
        val leftYSpace = 18.dp.toPx()
        val plotW = w - leftYSpace
        val plotH = h - bottomSpace

        val paint = android.graphics.Paint().apply {
            color = textPaintColor
            textSize = 10.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val yPaint = android.graphics.Paint().apply {
            color = textPaintColor
            textSize = 9.dp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        // Draw Y Axis labels (0, 1, 2 or max)
        val ySteps = listOf(0, (maxVal / 2).coerceAtLeast(1), maxVal)
        ySteps.forEach { step ->
            val yPos = plotH - (step.toFloat() / maxVal.toFloat()) * plotH
            drawContext.canvas.nativeCanvas.drawText(
                step.toString(),
                leftYSpace - 6.dp.toPx(),
                yPos + 3.dp.toPx(),
                yPaint
            )
            // Subtle dotted/solid guideline
            drawLine(
                color = Color.Gray.copy(alpha = 0.12f),
                start = Offset(leftYSpace, yPos),
                end = Offset(w, yPos),
                strokeWidth = 1.dp.toPx()
            )
        }

        val n = data.size
        val xStep = plotW / (n - 1).coerceAtLeast(1)

        val points = data.mapIndexed { idx, item ->
            val x = leftYSpace + idx * xStep
            val yFraction = (item.completedCount.toFloat() / maxVal.toFloat()) * animProgress.value
            val y = plotH - yFraction * plotH
            Offset(x, y)
        }

        // Build smooth curved path
        val path = Path()
        val fillPath = Path()

        if (points.isNotEmpty()) {
            path.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, plotH)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX = (p0.x + p1.x) / 2
                path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, plotH)
            fillPath.close()

            // Fill gradient below line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.18f), Color.Transparent),
                    startY = 0f,
                    endY = plotH
                )
            )

            // Draw line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Draw points, tooltips and X-axis labels
        var peakPoint: Offset? = null
        var peakVal = 0

        data.forEachIndexed { idx, item ->
            val pt = points[idx]
            // X-axis label
            drawContext.canvas.nativeCanvas.drawText(
                item.dayLabel,
                pt.x,
                h - 2.dp.toPx(),
                paint
            )

            if (item.completedCount > 0 && item.completedCount >= peakVal) {
                peakVal = item.completedCount
                peakPoint = pt
            }
        }

        // Peak indicator bubble matching Screen 4 ("1 done")
        peakPoint?.let { pt ->
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = lineColor,
                radius = 3.5.dp.toPx(),
                center = pt
            )

            // Tooltip bubble
            val bubbleText = "$peakVal done"
            val bubbleW = 44.dp.toPx()
            val bubbleH = 18.dp.toPx()
            val bubbleX = (pt.x - bubbleW / 2).coerceIn(leftYSpace, w - bubbleW)
            val bubbleY = pt.y - bubbleH - 8.dp.toPx()

            drawRoundRect(
                color = lineColor.copy(alpha = 0.15f),
                topLeft = Offset(bubbleX, bubbleY),
                size = Size(bubbleW, bubbleH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(bubbleH / 2)
            )

            val bubblePaint = android.graphics.Paint().apply {
                color = lineColor.toArgb()
                textSize = 9.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }

            drawContext.canvas.nativeCanvas.drawText(
                bubbleText,
                bubbleX + bubbleW / 2,
                bubbleY + bubbleH / 2 + 3.dp.toPx(),
                bubblePaint
            )
        }
    }
}

@Composable
fun CategoryDonutChartCard(
    categories: List<CategoryShareData>,
    totalTasks: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Donut Chart with total count in center
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(90.dp)) {
                val strokeWidth = 10.dp.toPx()
                val centerOffset = Offset(size.width / 2, size.height / 2)
                val radius = (size.minDimension - strokeWidth) / 2

                if (categories.isEmpty() || categories.all { it.totalCount == 0 }) {
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        style = Stroke(strokeWidth)
                    )
                } else {
                    var startAngle = -90f
                    val total = categories.sumOf { it.totalCount }.coerceAtLeast(1)

                    categories.forEach { cat ->
                        val sweep = (cat.totalCount.toFloat() / total.toFloat()) * 360f
                        val color = try {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        } catch (_: Exception) {
                            IndigoPrimary
                        }

                        if (sweep > 0f) {
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep - 2f, // subtle gap
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += sweep
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalTasks.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Right: Category legend list matching Screen 4
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val color = try {
                    Color(android.graphics.Color.parseColor(cat.colorHex))
                } catch (_: Exception) {
                    IndigoPrimary
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cat.categoryName,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${cat.completedCount}/${cat.totalCount}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
