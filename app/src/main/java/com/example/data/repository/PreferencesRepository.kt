package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AppColorTheme(
    val title: String,
    val primaryHex: Long,
    val primaryDarkHex: Long,
    val secondaryHex: Long
) {
    INDIGO("Royal Indigo", 0xFF5B4DF6, 0xFF7C6FF8, 0xFF10B981),
    SAPPHIRE("Ocean Sapphire", 0xFF2563EB, 0xFF3B82F6, 0xFF06B6D4),
    EMERALD("Emerald Mint", 0xFF059669, 0xFF10B981, 0xFF3B82F6),
    ROSE("Sunset Rose", 0xFFE11D48, 0xFFF43F5E, 0xFFF59E0B),
    CYAN("Cyber Cyan", 0xFF0891B2, 0xFF06B6D4, 0xFF8B5CF6),
    VIOLET("Deep Violet", 0xFF7C3AED, 0xFF8B5CF6, 0xFFEC4899),
    AMBER("Sunset Amber", 0xFFD97706, 0xFFF59E0B, 0xFF10B981)
}

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("taskflow_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _colorTheme = MutableStateFlow(loadColorTheme())
    val colorTheme: StateFlow<AppColorTheme> = _colorTheme.asStateFlow()

    private val _weeklyGoal = MutableStateFlow(loadWeeklyGoal())
    val weeklyGoal: StateFlow<Int> = _weeklyGoal.asStateFlow()

    private val _persistentAlertsEnabled = MutableStateFlow(loadPersistentAlerts())
    val persistentAlertsEnabled: StateFlow<Boolean> = _persistentAlertsEnabled.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val name = prefs.getString(KEY_THEME, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (_: Exception) {
            AppThemeMode.DARK
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadColorTheme(): AppColorTheme {
        val name = prefs.getString(KEY_COLOR_THEME, AppColorTheme.INDIGO.name) ?: AppColorTheme.INDIGO.name
        return try {
            AppColorTheme.valueOf(name)
        } catch (_: Exception) {
            AppColorTheme.INDIGO
        }
    }

    fun setColorTheme(theme: AppColorTheme) {
        prefs.edit().putString(KEY_COLOR_THEME, theme.name).apply()
        _colorTheme.value = theme
    }

    private fun loadWeeklyGoal(): Int {
        return prefs.getInt(KEY_WEEKLY_GOAL, 10)
    }

    fun setWeeklyGoal(goal: Int) {
        prefs.edit().putInt(KEY_WEEKLY_GOAL, goal).apply()
        _weeklyGoal.value = goal
    }

    private fun loadPersistentAlerts(): Boolean {
        return prefs.getBoolean(KEY_PERSISTENT_NOTIF, true)
    }

    fun setPersistentAlerts(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERSISTENT_NOTIF, enabled).apply()
        _persistentAlertsEnabled.value = enabled
    }

    companion object {
        private const val KEY_THEME = "key_theme_mode"
        private const val KEY_COLOR_THEME = "key_color_theme"
        private const val KEY_WEEKLY_GOAL = "key_weekly_goal"
        private const val KEY_PERSISTENT_NOTIF = "key_persistent_notif"
    }
}
