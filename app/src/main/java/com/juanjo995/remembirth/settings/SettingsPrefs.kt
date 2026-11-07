package com.juanjo995.remembirth.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * Ajustes guardados de la app (SharedPreferences: almacén clave-valor pequeño).
 * Todos los ajustes usan el mismo fichero "settings".
 */
internal fun Context.settingsPrefs(): SharedPreferences =
    getSharedPreferences("settings", Context.MODE_PRIVATE)
