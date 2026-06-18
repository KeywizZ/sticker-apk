package com.stickerapk.ui.roll

import com.stickerapk.data.StickerSheet

enum class RollPhase {
    PREVIEW,
    REVEALING,
    COMPLETE,
}

enum class RevealStep {
    FADING_PREVIEW,
    SPOTLIGHT,
    SETTLING,
}

data class FloatingSheet(
    val sheet: StickerSheet,
    val slot: Int,
)

data class RolledWord(
    val word: String,
    val vowelCount: Int,
    val sheetName: String,
    val accentHue: Float,
)

data class RollUiState(
    val phase: RollPhase = RollPhase.PREVIEW,
    val floatingSheets: List<FloatingSheet> = emptyList(),
    val chosenSheets: List<StickerSheet> = emptyList(),
    val revealedSheets: List<StickerSheet> = emptyList(),
    val revealStep: RevealStep = RevealStep.FADING_PREVIEW,
    val spotlightIndex: Int = 0,
    val previewFadeOut: Boolean = false,
    val rolledWords: List<RolledWord> = emptyList(),
    val pickedWinner: RolledWord? = null,
) {
    val winnerWordNames: Set<String> by lazy {
        pickedWinner?.word?.let { setOf(it) } ?: emptySet()
    }
}
