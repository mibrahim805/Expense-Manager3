package com.expensemanager.android.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.expensemanager.android.core.designsystem.FinanceThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.themeDataStore by preferencesDataStore(name = "rupeyawise_preferences")

class ThemePreferences(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")

    val theme: Flow<FinanceThemeMode> = context.themeDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            preferences[themeKey]
                ?.let { value -> runCatching { FinanceThemeMode.valueOf(value) }.getOrNull() }
                ?: FinanceThemeMode.MIDNIGHT
        }

    suspend fun setTheme(mode: FinanceThemeMode) {
        context.themeDataStore.edit { preferences -> preferences[themeKey] = mode.name }
    }
}
