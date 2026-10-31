package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ble.HeartRatePoint
import com.example.ui.theme.VeilCyanAccent

@Composable
fun HeartRateTrendChart(
    points: List<HeartRatePoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val lineColor = VeilCyanAccent
    val surfaceBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        val width = size.width
        val height = size.height

        val bpms = points.map { it.bpm }
        val minBpm = (bpms.minOrNull() ?: 60).coerceAtMost(55)
        val maxBpm = (bpms.maxOrNull() ?: 100).coerceAtLeast(110)
        val bpmRange = (maxBpm - minBpm).coerceAtLeast(20)

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = surfaceBorder,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (points.size < 2) {
            // Draw single point if only 1 sample
            val p = points.first()
            val y = height - ((p.bpm - minBpm).toFloat() / bpmRange * height)
            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = Offset(width / 2f, y)
            )
            return@Canvas
        }

        val stepX = width / (points.size - 1)
        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, item ->
            val x = index * stepX
            val normalizedY = (item.bpm - minBpm).toFloat() / bpmRange
            val y = (height - (normalizedY * height)).coerceIn(0f, height)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * stepX
                val prevNormY = (points[index - 1].bpm - minBpm).toFloat() / bpmRange
                val prevY = (height - (prevNormY * height)).coerceIn(0f, height)

                // Smooth cubic Bezier curve between points
                val cX1 = prevX + (x - prevX) / 2f
                val cY1 = prevY
                val cX2 = prevX + (x - prevX) / 2f
                val cY2 = y

                path.cubicTo(cX1, cY1, cX2, cY2, x, y)
                fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw soft gradient area fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.35f),
                    lineColor.copy(alpha = 0.05f),
                    Color.Transparent
                )
            )
        )

        // Draw trend line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw glowing marker on latest point
        val lastIndex = points.size - 1
        val lastX = lastIndex * stepX
        val lastNormY = (points[lastIndex].bpm - minBpm).toFloat() / bpmRange
        val lastY = (height - (lastNormY * height)).coerceIn(0f, height)

        drawCircle(
            color = lineColor.copy(alpha = 0.3f),
            radius = 9.dp.toPx(),
            center = Offset(lastX, lastY)
        )
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}
