package com.stickerapk.data

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM,
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
)
