package com.stickerapk.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stickerapk.data.Sticker
import com.stickerapk.data.StickerSheet

@Composable
fun StickerAssetImage(
    assetPath: String,
    label: String,
    accentHue: Float,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    subtitle: String? = null,
) {
    val context = LocalContext.current
    val bitmap = remember(assetPath) {
        if (assetPath.isBlank()) {
            null
        } else {
            runCatching {
                context.assets.open(assetPath).use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = label,
            modifier = modifier.clip(RoundedCornerShape(16.dp)),
            contentScale = contentScale,
        )
    } else {
        WordPlaceholderArt(
            label = label,
            accentHue = accentHue,
            subtitle = subtitle,
            modifier = modifier,
        )
    }
}

@Composable
fun WordPlaceholderArt(
    label: String,
    accentHue: Float,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val base = Color.hsv(accentHue, 0.55f, 0.85f)
    val glow = Color.hsv((accentHue + 40f) % 360f, 0.45f, 0.95f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(base.copy(alpha = 0.9f), glow.copy(alpha = 0.75f)),
                ),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (label.length > 14) 15.sp else 18.sp,
                ),
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun PlaceholderArt(
    label: String,
    accentHue: Float,
    modifier: Modifier = Modifier,
) {
    WordPlaceholderArt(
        label = label,
        accentHue = accentHue,
        subtitle = null,
        modifier = modifier,
    )
}

@Composable
fun AnimatedFloatingSheet(
    sheet: StickerSheet,
    slot: Int,
    modifier: Modifier = Modifier,
) {
    val accentHue = remember(sheet.id) { (sheet.id.hashCode() % 360).toFloat().let { if (it < 0) it + 360 else it } }
    val rotation = remember(sheet.id, slot) { -8f + slot * 7f + (sheet.id.hashCode() % 11) }
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
            },
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
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = sheet.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun RevealedStickerCard(
    sticker: Sticker,
    index: Int,
    modifier: Modifier = Modifier,
) {
    var entered by remember(sticker.id) { mutableStateOf(false) }
    LaunchedEffect(sticker.id) { entered = true }

    val animatedScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.55f,
        animationSpec = tween(durationMillis = 420),
        label = "stickerScale",
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 380),
        label = "stickerAlpha",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale * 0.85f + 0.15f
                scaleY = animatedScale * 0.85f + 0.15f
                alpha = animatedAlpha
                rotationZ = ((index % 3) - 1) * 2.5f
            }
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        StickerAssetImage(
            assetPath = sticker.assetPath,
            label = sticker.name,
            accentHue = sticker.accentHue,
            subtitle = if (sticker.vowels > 0) "${sticker.vowels} vowels" else null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}
