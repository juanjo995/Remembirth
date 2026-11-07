package com.juanjo995.remembirth.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.juanjo995.remembirth.R

/**
 * Tema de la app (claro/oscuro) y cómo se guarda.
 *
 * Se aplica con AppCompatDelegate.setDefaultNightMode: Android cambia el modo
 * noche solo para esta app y Compose lo detecta con isSystemInDarkTheme(),
 * así que RemembirthTheme no necesita ningún cambio.
 */
enum class ThemeMode(val nightMode: Int, @param:StringRes val labelRes: Int) {
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.theme_system),
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO, R.string.theme_light),
    DARK(AppCompatDelegate.MODE_NIGHT_YES, R.string.theme_dark);

    companion object {
        private const val KEY_THEME = "theme_mode"

        /** Tema guardado (por defecto, el del sistema). */
        fun load(context: Context): ThemeMode {
            val saved = context.settingsPrefs().getString(KEY_THEME, null)
            return entries.firstOrNull { it.name == saved } ?: SYSTEM
        }

        /** Guarda el tema y lo aplica (la pantalla se redibuja con los nuevos colores). */
        fun save(context: Context, mode: ThemeMode) {
            context.settingsPrefs().edit {
                putString(KEY_THEME, mode.name)
            }
            AppCompatDelegate.setDefaultNightMode(mode.nightMode)
        }
    }
}
