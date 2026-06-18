package com.stickerapk.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    val drift: Float,
    val fallSpeed: Float,
    val spin: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val delay: Float,
)

private val CONFETTI_COLORS = listOf(
    Color(0xFF8B5CF6),
    Color(0xFF22D3EE),
    Color(0xFFF472B6),
    Color(0xFFFBBF24),
    Color(0xFF34D399),
    Color(0xFFFB7185),
    Color(0xFF60A5FA),
)

@Composable
fun ConfettiEffect(
    active: Boolean,
    triggerKey: Any?,
    modifier: Modifier = Modifier,
) {
    if (!active) return

    val pieces = remember {
        List(72) {
            ConfettiPiece(
                startX = Random.nextFloat(),
                startY = Random.nextFloat() * -0.35f,
                drift = Random.nextFloat() * 0.18f - 0.09f,
                fallSpeed = 0.45f + Random.nextFloat() * 0.55f,
                spin = Random.nextFloat() * 720f - 360f,
                width = 6f + Random.nextFloat() * 8f,
                height = 10f + Random.nextFloat() * 14f,
                color = CONFETTI_COLORS.random(),
                delay = Random.nextFloat() * 0.35f,
            )
        }
    }

    var progress by remember(triggerKey) { mutableFloatStateOf(0f) }
    var opacity by remember(triggerKey) { mutableFloatStateOf(1f) }
    var visible by remember(triggerKey) { mutableStateOf(true) }

    LaunchedEffect(triggerKey) {
        progress = 0f
        opacity = 1f
        visible = true

        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800),
        ) { value, _ ->
            progress = value
        }

        animate(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = tween(durationMillis = 700),
        ) { value, _ ->
            opacity = value
        }

        visible = false
    }

    if (!visible) return

    Canvas(modifier = modifier) {
        pieces.forEach { piece ->
            val localT = ((progress - piece.delay).coerceIn(0f, 1f))
            if (localT <= 0f) return@forEach

            val pieceFade = when {
                localT > 0.75f -> 1f - ((localT - 0.75f) / 0.25f)
                else -> 1f
            }

            val x = (piece.startX + piece.drift * localT + sin(localT * 12f) * 0.03f) * size.width
            val y = (piece.startY + piece.fallSpeed * localT) * size.height

            if (y > size.height + 40f) return@forEach

            rotate(degrees = piece.spin * localT, pivot = Offset(x, y)) {
                drawRect(
                    color = piece.color.copy(alpha = pieceFade * opacity),
                    topLeft = Offset(x - piece.width / 2f, y - piece.height / 2f),
                    size = Size(piece.width, piece.height),
                )
            }
        }
    }
}
