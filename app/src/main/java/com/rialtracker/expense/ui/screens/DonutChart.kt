package com.rialtracker.expense.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class DonutSlice(val value: Float, val color: Color, val label: String)

/** نمودار دونات ساده، بدون وابستگی خارجی؛ برای نمایش سهم هر دسته‌بندی از کل مخارج */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidthDp: Float = 28f,
    centerContent: @Composable () -> Unit = {}
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()

    Box(modifier = modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(180.dp)) {
            if (total <= 0f) return@Canvas
            var startAngle = -90f
            val strokePx = strokeWidthDp * density
            slices.forEach { slice ->
                val sweep = (slice.value / total) * 360f
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep.coerceAtLeast(0.5f),
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        centerContent()
    }
}
