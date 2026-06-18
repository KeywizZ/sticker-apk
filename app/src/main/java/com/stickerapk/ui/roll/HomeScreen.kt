package com.stickerapk.ui.roll

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stickerapk.data.VowelUtils
import com.stickerapk.ui.components.ConfettiEffect

@Composable
fun HomeScreen(
    uiState: RollUiState,
    onRoll: () -> Unit,
    onReset: () -> Unit,
    onOpenOptions: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val glowShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowShift",
    )

    val backgroundBrush = Brush.radialGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f + glowShift * 0.08f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
        radius = 900f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            HeaderRow(onOpenOptions = onOpenOptions)

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = uiState.phase == RollPhase.PREVIEW,
                enter = fadeIn(tween(250)) + expandVertically(tween(250)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
            ) {
                StatusSubtitle("Sheets are shuffling… tap roll when ready.")
            }

            AnimatedVisibility(
                visible = uiState.phase == RollPhase.COMPLETE && uiState.pickedWinner != null,
                enter = fadeIn(tween(420, easing = FastOutSlowInEasing)) +
                    expandVertically(
                        animationSpec = tween(520, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Top,
                    ) +
                    slideInVertically(
                        animationSpec = tween(520, easing = FastOutSlowInEasing),
                        initialOffsetY = { fullHeight -> -fullHeight / 2 },
                    ),
                exit = fadeOut(tween(220)) +
                    shrinkVertically(
                        animationSpec = tween(220),
                        shrinkTowards = Alignment.Top,
                    ) +
                    slideOutVertically { fullHeight -> -fullHeight / 2 },
            ) {
                WinnerBanner(uiState = uiState)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                    .padding(16.dp),
            ) {
                RollRevealStage(
                    uiState = uiState,
                    wordsContent = {
                        AnimatedVisibility(
                            visible = uiState.phase == RollPhase.COMPLETE &&
                                uiState.rolledWords.isNotEmpty(),
                            enter = fadeIn(tween(420, delayMillis = 180, easing = FastOutSlowInEasing)) +
                                expandVertically(
                                    animationSpec = tween(480, delayMillis = 180, easing = FastOutSlowInEasing),
                                    expandFrom = Alignment.Top,
                                ) +
                                slideInVertically(
                                    animationSpec = tween(480, delayMillis = 180, easing = FastOutSlowInEasing),
                                    initialOffsetY = { fullHeight -> -fullHeight / 3 },
                                ),
                        ) {
                            WordsResultsPanel(
                                words = uiState.rolledWords,
                                winnerNames = uiState.winnerWordNames,
                            )
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ActionRow(
                phase = uiState.phase,
                onRoll = onRoll,
                onReset = onReset,
            )
        }

        val winner = uiState.pickedWinner
        ConfettiEffect(
            active = uiState.phase == RollPhase.COMPLETE &&
                winner?.vowelCount == VowelUtils.MAX_DISTINCT_VOWELS,
            triggerKey = winner?.word,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(420.dp),
        )
    }
}

@Composable
private fun StatusSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun WinnerBanner(uiState: RollUiState) {
    val winner = uiState.pickedWinner
    if (winner == null) {
        StatusSubtitle("Your draw is ready.")
        return
    }

    val pulseTransition = rememberInfiniteTransition(label = "winnerPulse")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "winnerGlow",
    )
    val scale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "winnerScale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f + glowAlpha * 0.15f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f + glowAlpha * 0.12f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                    ),
                ),
            )
            .padding(2.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha),
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "${winner.word}: You get",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha),
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = "${winner.vowelCount}",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                ),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f + glowAlpha * 0.15f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun HeaderRow(onOpenOptions: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Sticker Roll",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Pick three sheets — highest vowel count wins!",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        IconButton(onClick = onOpenOptions) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun WordsResultsPanel(
    words: List<RolledWord>,
    winnerNames: Set<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "All 9 words",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Distinct vowels (a, e, i, o, u, y) — duplicates don't count.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        words.forEach { rolled ->
            val isWinner = rolled.word in winnerNames
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isWinner) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                        },
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rolled.word,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (isWinner) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = rolled.sheetName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${rolled.vowelCount}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isWinner) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun ActionRow(
    phase: RollPhase,
    onRoll: () -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (phase == RollPhase.COMPLETE) {
            Button(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Shuffle again")
            }
        } else {
            Button(
                onClick = onRoll,
                enabled = phase == RollPhase.PREVIEW,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = if (phase == RollPhase.REVEALING) "Rolling…" else "Roll 3 sheets",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}
