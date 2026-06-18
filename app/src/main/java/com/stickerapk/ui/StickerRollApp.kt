package com.stickerapk.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stickerapk.data.UserPreferences
import com.stickerapk.data.UserPreferencesRepository
import com.stickerapk.ui.options.OptionsScreen
import com.stickerapk.ui.roll.HomeScreen
import com.stickerapk.ui.roll.RollViewModel
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val OPTIONS = "options"
}

@Composable
fun StickerRollApp(
    rollViewModel: RollViewModel,
    preferencesRepository: UserPreferencesRepository,
    preferences: UserPreferences,
) {
    val navController = rememberNavController()
    val rollState by rollViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    uiState = rollState,
                    onRoll = rollViewModel::rollSheets,
                    onReset = rollViewModel::resetToPreview,
                    onOpenOptions = { navController.navigate(Routes.OPTIONS) },
                )
            }
            composable(Routes.OPTIONS) {
                OptionsScreen(
                    preferences = preferences,
                    onThemeModeChange = { mode ->
                        scope.launch { preferencesRepository.setThemeMode(mode) }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
