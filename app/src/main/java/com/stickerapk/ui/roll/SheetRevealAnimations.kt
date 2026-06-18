package com.stickerapk.ui.roll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stickerapk.data.StickerSheet
import com.stickerapk.ui.components.AnimatedFloatingSheet
import com.stickerapk.ui.components.StickerAssetImage

@Composable
fun PreviewStage(
    floatingSheets: List<FloatingSheet>,
    fadeOut: Boolean,
    modifier: Modifier = Modifier,
) {
    val fadeAlpha by animateFloatAsState(
        targetValue = if (fadeOut) 0f else 1f,
        animationSpec = tween(durationMillis = 450),
        label = "previewFadeOut",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fadeAlpha },
    ) {
        val positions = listOf(
            Alignment.TopStart to Modifier.padding(start = 8.dp, top = 12.dp).size(132.dp),
            Alignment.Center to Modifier.size(156.dp),
            Alignment.BottomEnd to Modifier.padding(end = 8.dp, bottom = 12.dp).size(128.dp),
        )

        floatingSheets.forEachIndexed { index, floating ->
            val (alignment, sizeModifier) = positions.getOrElse(index) {
                Alignment.Center to Modifier.size(140.dp)
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = alignment,
            ) {
                AnimatedFloatingSheet(
                    sheet = floating.sheet,
                    slot = floating.slot,
                    modifier = sizeModifier,
                )
            }
        }

        if (floatingSheets.isEmpty() && fadeAlpha > 0.01f) {
            Text(
                text = "Loading sheets…",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
fun RollRevealStage(
    uiState: RollUiState,
    modifier: Modifier = Modifier,
    wordsContent: @Composable ColumnScope.() -> Unit = {},
) {
    val settleProgress = remember { Animatable(0f) }

    LaunchedEffect(uiState.revealStep) {
        when (uiState.revealStep) {
            RevealStep.SETTLING -> {
                settleProgress.snapTo(0f)
                settleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                )
            }
            RevealStep.FADING_PREVIEW, RevealStep.SPOTLIGHT -> {
                settleProgress.snapTo(0f)
            }
        }
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == RollPhase.COMPLETE) {
            settleProgress.snapTo(1f)
        }
    }

    val previewVisible = uiState.phase == RollPhase.PREVIEW ||
        (uiState.revealStep == RevealStep.FADING_PREVIEW && uiState.floatingSheets.isNotEmpty())
    val revealVisible = uiState.revealStep == RevealStep.SPOTLIGHT ||
        uiState.revealStep == RevealStep.SETTLING ||
        uiState.phase == RollPhase.COMPLETE

    Box(modifier = modifier.fillMaxSize()) {
        if (previewVisible) {
            PreviewStage(
                floatingSheets = uiState.floatingSheets,
                fadeOut = uiState.previewFadeOut,
            )
        }

        if (revealVisible && uiState.chosenSheets.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (uiState.phase == RollPhase.COMPLETE) {
                            Modifier.verticalScroll(rememberScrollState())
                        } else {
                            Modifier
                        },
                    ),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            ) {
                UnifiedSheetReveal(
                    sheets = uiState.chosenSheets,
                    spotlightIndex = uiState.spotlightIndex,
                    revealStep = uiState.revealStep,
                    phase = uiState.phase,
                    settleProgress = settleProgress.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (uiState.phase == RollPhase.COMPLETE) {
                                Modifier.height(132.dp)
                            } else {
                                Modifier.weight(1f)
                            },
                        ),
                )

                if (uiState.phase == RollPhase.COMPLETE) {
                    wordsContent()
                }
            }
        }
    }
}

@Composable
private fun UnifiedSheetReveal(
    sheets: List<StickerSheet>,
    spotlightIndex: Int,
    revealStep: RevealStep,
    phase: RollPhase,
    settleProgress: Float,
    modifier: Modifier = Modifier,
) {
    val isSettling = revealStep == RevealStep.SETTLING || phase == RollPhase.COMPLETE
    val progress = if (isSettling) settleProgress.coerceIn(0f, 1f) else 0f
    val activeSpotlightIndex = spotlightIndex.coerceIn(0, (sheets.size - 1).coerceAtLeast(0))

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val gap = 8.dp
        val slotWidth = (maxWidth - gap * (sheets.size - 1).coerceAtLeast(0)) /
            sheets.size.coerceAtLeast(1)
        val rowHeight = 132.dp
        val stageHeight = if (phase == RollPhase.COMPLETE) rowHeight else maxHeight
        val largeWidth = scaleDp(maxWidth, 0.88f)
        val largeHeight = scaleDp(stageHeight, 0.88f)
        val spotlightLeft = (maxWidth - largeWidth) / 2
        val spotlightTop = (stageHeight - largeHeight) / 2

        if (progress >= 0.999f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                sheets.forEach { sheet ->
                    LargeSheetCard(
                        sheet = sheet,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(stageHeight),
            ) {
                sheets.forEachIndexed { index, sheet ->
                    AnimatedRevealSheet(
                        index = index,
                        sheet = sheet,
                        activeSpotlightIndex = activeSpotlightIndex,
                        revealStep = revealStep,
                        progress = progress,
                        spotlightLeft = spotlightLeft,
                        spotlightTop = spotlightTop,
                        slotWidth = slotWidth,
                        slotLeft = (slotWidth + gap) * index,
                        rowHeight = rowHeight,
                        largeWidth = largeWidth,
                        largeHeight = largeHeight,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedRevealSheet(
    index: Int,
    sheet: StickerSheet,
    activeSpotlightIndex: Int,
    revealStep: RevealStep,
    progress: Float,
    spotlightLeft: Dp,
    spotlightTop: Dp,
    slotWidth: Dp,
    slotLeft: Dp,
    rowHeight: Dp,
    largeWidth: Dp,
    largeHeight: Dp,
) {
    val entrance = remember(sheet.id) { Animatable(0f) }

    LaunchedEffect(revealStep, activeSpotlightIndex, index) {
        when {
            revealStep == RevealStep.SPOTLIGHT && index == activeSpotlightIndex -> {
                entrance.snapTo(0f)
                entrance.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
                )
            }
            revealStep == RevealStep.SETTLING && index < activeSpotlightIndex -> {
                entrance.snapTo(0f)
                entrance.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                )
            }
        }
    }

    val targetAlpha = when {
        revealStep == RevealStep.SPOTLIGHT -> if (index == activeSpotlightIndex) 1f else 0f
        progress <= 0f -> if (index == activeSpotlightIndex) 1f else 0f
        index == activeSpotlightIndex -> 1f
        else -> progress.coerceIn(0f, 1f)
    }
    val sheetAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 320),
        label = "sheetAlpha$index",
    )

    if (sheetAlpha <= 0.01f && entrance.value <= 0.01f) return

    val currentLeft = lerp(spotlightLeft, slotLeft, progress)
    val baseTop = lerp(spotlightTop, 0.dp, progress)
    val currentWidth = lerp(largeWidth, slotWidth, progress)
    val currentHeight = lerp(largeHeight, rowHeight, progress)
    val rollDrop = lerp(currentHeight, 0.dp, entrance.value)
    val rollRotation = lerpFloat(-18f, 0f, entrance.value)

    LargeSheetCard(
        sheet = sheet,
        modifier = Modifier
            .graphicsLayer {
                alpha = sheetAlpha
                rotationZ = rollRotation
            }
            .offset(x = currentLeft, y = baseTop - rollDrop)
            .size(currentWidth, currentHeight),
    )
}

@Composable
private fun LargeSheetCard(
    sheet: StickerSheet,
    modifier: Modifier = Modifier,
) {
    val accentHue = remember(sheet.id) {
        (sheet.id.hashCode() % 360).toFloat().let { if (it < 0) it + 360 else it }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        StickerAssetImage(
            assetPath = sheet.assetPath,
            label = sheet.name,
            accentHue = accentHue,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = sheet.name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun scaleDp(value: Dp, fraction: Float): Dp =
    (value.value * fraction).dp

private fun lerp(start: Dp, end: Dp, fraction: Float): Dp =
    (start.value + (end.value - start.value) * fraction).dp

private fun lerpFloat(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction
