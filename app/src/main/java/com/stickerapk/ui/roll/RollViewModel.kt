package com.stickerapk.ui.roll

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stickerapk.data.StickerRepository
import com.stickerapk.data.VowelUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RollViewModel(
    private val repository: StickerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RollUiState())
    val uiState: StateFlow<RollUiState> = _uiState.asStateFlow()

    private var previewJob: Job? = null
    private var revealJob: Job? = null

    init {
        startPreviewLoop()
    }

    fun rollSheets() {
        if (_uiState.value.phase == RollPhase.REVEALING) return

        previewJob?.cancel()
        revealJob?.cancel()

        val chosenSheets = repository.rollSheets(SHEETS_PER_ROLL)

        _uiState.update {
            it.copy(
                phase = RollPhase.REVEALING,
                revealStep = RevealStep.FADING_PREVIEW,
                previewFadeOut = true,
                chosenSheets = chosenSheets,
                revealedSheets = emptyList(),
                spotlightIndex = 0,
                rolledWords = emptyList(),
                pickedWinner = null,
            )
        }

        revealJob = viewModelScope.launch {
            delay(PREVIEW_FADE_MS)

            _uiState.update {
                it.copy(
                    revealStep = RevealStep.SPOTLIGHT,
                    spotlightIndex = 0,
                )
            }

            delay(SPOTLIGHT_MS)

            _uiState.update { it.copy(spotlightIndex = 1) }
            delay(SPOTLIGHT_MS)

            _uiState.update { it.copy(spotlightIndex = 2) }
            delay(SPOTLIGHT_MS)

            _uiState.update {
                it.copy(
                    revealStep = RevealStep.SETTLING,
                    revealedSheets = chosenSheets,
                    floatingSheets = emptyList(),
                )
            }

            delay(SETTLE_MS)

            val rolledWords = repository.stickersForSheets(chosenSheets).map { sticker ->
                val sheetName = chosenSheets.firstOrNull { it.id == sticker.sheetId }?.name ?: sticker.sheetId
                RolledWord(
                    word = sticker.name,
                    vowelCount = VowelUtils.countDistinctVowels(sticker.name),
                    sheetName = sheetName,
                    accentHue = sticker.accentHue,
                )
            }

            val maxVowels = rolledWords.maxOfOrNull { it.vowelCount } ?: 0
            val topWords = rolledWords.filter { it.vowelCount == maxVowels }
            val pickedWinner = topWords.random()

            _uiState.update {
                it.copy(
                    phase = RollPhase.COMPLETE,
                    rolledWords = rolledWords,
                    pickedWinner = pickedWinner,
                )
            }
        }
    }

    fun resetToPreview() {
        revealJob?.cancel()
        _uiState.update { RollUiState() }
        startPreviewLoop()
    }

    private fun startPreviewLoop() {
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            while (true) {
                val sheets = repository.pickRandomSheets(PREVIEW_SHEET_COUNT)
                _uiState.update { state ->
                    if (state.phase != RollPhase.PREVIEW) {
                        return@update state
                    }
                    state.copy(
                        floatingSheets = sheets.mapIndexed { index, sheet ->
                            FloatingSheet(sheet = sheet, slot = index)
                        },
                    )
                }
                delay(PREVIEW_CYCLE_MS)
            }
        }
    }

    override fun onCleared() {
        previewJob?.cancel()
        revealJob?.cancel()
        super.onCleared()
    }

    companion object {
        const val SHEETS_PER_ROLL = 3
        private const val PREVIEW_SHEET_COUNT = 3
        private const val PREVIEW_CYCLE_MS = 900L
        private const val PREVIEW_FADE_MS = 500L
        private const val SPOTLIGHT_MS = 750L
        private const val SETTLE_MS = 900L
    }
}

class RollViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RollViewModel::class.java)) {
            return RollViewModel(StickerRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
