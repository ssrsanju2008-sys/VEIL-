package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VeilEmerald
import com.example.ui.theme.VeilEmeraldBright
import com.example.ui.theme.VeilEmeraldDark
import com.example.ui.theme.VeilEmergencyRed

@Composable
fun StatusRing(
    isConnected: Boolean,
    isAlertActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val activeColor = when {
        isAlertActive -> VeilEmergencyRed
        isConnected -> VeilEmerald
        else -> MaterialTheme.colorScheme.outline
    }

    val glowColor = when {
        isAlertActive -> Color(0x33EF4444)
        isConnected -> Color(0x2210B981)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .size(230.dp)
            .testTag("status_ring_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing safety aura
        if (isConnected || isAlertActive) {
            Canvas(
                modifier = Modifier
                    .size(220.dp * pulseScale)
                    .clip(CircleShape)
            ) {
                drawCircle(
                    color = glowColor,
                    radius = size.minDimension / 2f
                )
            }
        }

        // Circular Status Stroke Ring
        Canvas(modifier = Modifier.size(190.dp)) {
            val strokeWidth = 8.dp.toPx()
            // Track background ring
            drawCircle(
                color = activeColor.copy(alpha = 0.15f),
                radius = (size.minDimension - strokeWidth) / 2f,
                style = Stroke(width = strokeWidth)
            )

            // Dynamic progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        activeColor.copy(alpha = 0.4f),
                        activeColor,
                        if (isConnected) VeilEmeraldBright else activeColor
                    )
                ),
                startAngle = -90f,
                sweepAngle = if (isConnected || isAlertActive) 360f else 60f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Central Shield / Status Card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = when {
                    isAlertActive -> Icons.Default.Warning
                    isConnected -> Icons.Default.Security
                    else -> Icons.Default.BluetoothDisabled
                },
                contentDescription = "Status Icon",
                tint = activeColor,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when {
                    isAlertActive -> "ALERT"
                    isConnected -> "Protected"
                    else -> "Not Connected"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = when {
                    isAlertActive -> VeilEmergencyRed
                    isConnected -> VeilEmerald
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Text(
                text = when {
                    isAlertActive -> "EMERGENCY ACTIVE"
                    isConnected -> "VEIL Link Active"
                    else -> "Wristband Offline"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
