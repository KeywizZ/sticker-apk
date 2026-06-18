package com.stickerapk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stickerapk.data.UserPreferencesRepository
import com.stickerapk.ui.StickerRollApp
import com.stickerapk.ui.theme.StickerApkTheme
import com.stickerapk.ui.roll.RollViewModel
import com.stickerapk.ui.roll.RollViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesRepository = UserPreferencesRepository(applicationContext)

        setContent {
            val preferences by preferencesRepository.preferencesFlow.collectAsState(
                initial = com.stickerapk.data.UserPreferences(),
            )
            val useDarkTheme = when (preferences.themeMode) {
                com.stickerapk.data.ThemeMode.DARK -> true
                com.stickerapk.data.ThemeMode.LIGHT -> false
                com.stickerapk.data.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            StickerApkTheme(darkTheme = useDarkTheme) {
                val rollViewModel: RollViewModel = viewModel(
                    factory = RollViewModelFactory(applicationContext),
                )
                StickerRollApp(
                    rollViewModel = rollViewModel,
                    preferencesRepository = preferencesRepository,
                    preferences = preferences,
                )
            }
        }
    }
}
