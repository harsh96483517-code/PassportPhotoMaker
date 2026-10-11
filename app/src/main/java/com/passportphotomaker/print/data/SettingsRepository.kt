package com.passportphotomaker.print.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultPaper: PaperSize = PaperSize.R4,
    val defaultQuality: PrintQuality = PrintQuality.HIGH,
    val showFaceGuide: Boolean = true,
    val onboardingDone: Boolean = false
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val PAPER = stringPreferencesKey("default_paper")
        val QUALITY = stringPreferencesKey("default_quality")
        val GUIDE = booleanPreferencesKey("show_face_guide")
        val ONBOARDING = booleanPreferencesKey("onboarding_done")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = runCatching { ThemeMode.valueOf(p[Keys.THEME] ?: ThemeMode.SYSTEM.name) }
                .getOrDefault(ThemeMode.SYSTEM),
            defaultPaper = runCatching { PaperSize.valueOf(p[Keys.PAPER] ?: PaperSize.R4.name) }
                .getOrDefault(PaperSize.R4),
            defaultQuality = runCatching {
                PrintQuality.valueOf(p[Keys.QUALITY] ?: PrintQuality.HIGH.name)
            }.getOrDefault(PrintQuality.HIGH),
            showFaceGuide = p[Keys.GUIDE] ?: true,
            onboardingDone = p[Keys.ONBOARDING] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME] = mode.name }

    suspend fun setDefaultPaper(paper: PaperSize) =
        context.dataStore.edit { it[Keys.PAPER] = paper.name }

    suspend fun setDefaultQuality(quality: PrintQuality) =
        context.dataStore.edit { it[Keys.QUALITY] = quality.name }

    suspend fun setShowFaceGuide(show: Boolean) =
        context.dataStore.edit { it[Keys.GUIDE] = show }

    suspend fun setOnboardingDone(done: Boolean) =
        context.dataStore.edit { it[Keys.ONBOARDING] = done }
}
