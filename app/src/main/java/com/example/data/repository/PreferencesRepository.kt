package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("taskflow_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

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
        private const val KEY_WEEKLY_GOAL = "key_weekly_goal"
        private const val KEY_PERSISTENT_NOTIF = "key_persistent_notif"
    }
}
