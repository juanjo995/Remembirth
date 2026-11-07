package com.juanjo995.remembirth.birthday

import android.content.res.Resources
import android.text.TextUtils
import com.juanjo995.remembirth.R
import java.util.Locale

/**
 * Construye la frase de un cumpleaños ("🎉 Hoy Ana cumple 30 años",
 * "Mañana...", "En X días...") en el idioma de [resources], en HTML: <b> para la negrita y
 * <font color> para el color de "Hoy"/"Mañana".
 *
 * La usan tanto la lista (Compose) como las notificaciones, así que
 * las dos muestran exactamente el mismo texto.
 *
 * @param resources recursos en el idioma deseado.
 * @param darkBackground si el texto se mostrará sobre fondo oscuro (ajusta los colores).
 */
fun birthdayHtml(
    resources: Resources,
    name: String,
    birthday: UpcomingBirthday,
    darkBackground: Boolean,
): String {
    val days = birthday.daysUntil
    val age = birthday.age
    val template = when (days) {
        0 -> R.string.birthday_today
        1 -> R.string.birthday_tomorrow
        else -> R.string.birthday_in_days
    }
    val highlightColor = when (days) {
        0 -> if (darkBackground) TODAY_COLOR_DARK else TODAY_COLOR_LIGHT
        1 -> if (darkBackground) TOMORROW_COLOR_DARK else TOMORROW_COLOR_LIGHT
        else -> 0x000000 // "En X días": la frase no usa color
    }
    // "N días" y "N años" son plurales independientes: en muchos idiomas la palabra
    // cambia según el número (ruso: 2 дня / 5 дней), así que cada uno usa su propio número
    val daysPhrase = resources.getQuantityString(R.plurals.days_phrase, days, days)
    val agePhrase = resources.getQuantityString(R.plurals.age_phrase, age, age)
    // El nombre se "escapa" para que un nombre con < o & no rompa el HTML
    val safeName = TextUtils.htmlEncode(name)
    // Cada idioma coloca las piezas en su orden: %1$s días, %2$s nombre, %3$s años, %4$s color
    return resources.getString(template, daysPhrase, safeName, agePhrase, highlightColor.toHex())
}

// Rojo para "Hoy": un poco más claro sobre fondo oscuro para que se lea bien
private const val TODAY_COLOR_LIGHT = 0xD32F2F
private const val TODAY_COLOR_DARK = 0xEF5350
// Amarillo para "Mañana": sobre fondo claro, más oscuro (el amarillo puro no se lee sobre blanco)
private const val TOMORROW_COLOR_LIGHT = 0xF9A825
private const val TOMORROW_COLOR_DARK = 0xFDD835

/** Color en formato "#RRGGBB", que es lo que entiende <font color> del HTML. */
private fun Int.toHex(): String = String.format(Locale.ROOT, "#%06X", this and 0xFFFFFF)
