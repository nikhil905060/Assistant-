package com.astute.ai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun PlasmaOrbView(isListening: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    Canvas(modifier = modifier.size(280.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = (size.minDimension / 2) * if (isListening) scalePulse else 0.95f

        rotate(degrees = rotation, pivot = center) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFBA68C8),
                        Color(0xFF7B1FA2),
                        Color(0xFF38006B),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )
        }

        rotate(degrees = -rotation * 1.6f, pivot = center) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xEEEA80FC),
                        Color(0x66AB47BC),
                        Color.Transparent
                    ),
                    center = Offset(center.x + 18f, center.y - 18f),
                    radius = baseRadius * 0.72f
                ),
                radius = baseRadius * 0.72f,
                center = center
            )
        }
    }
}
