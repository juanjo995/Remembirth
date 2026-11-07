package com.juanjo995.remembirth.birthday

import java.util.TimeZone

private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

/**
 * Días que faltan para el próximo cumpleaños.
 *
 * - Devuelve 0 si el cumpleaños es hoy.
 * - Nacidos el 29 de febrero: en los años no bisiestos lo celebran el 28 de febrero.
 *
 * @param birthDateMillis fecha de nacimiento tal como se guarda en la base de datos
 *   (milisegundos UTC de la medianoche de ese día, lo que devuelve el DatePicker).
 * @param nowMillis instante actual. Por defecto, ahora.
 * @param timeZone zona horaria para saber qué día es "hoy". Por defecto, la del móvil.
 */
fun daysUntilBirthday(
    birthDateMillis: Long,
    nowMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault(),
): Int {
    // La fecha de nacimiento se guardó en UTC: se pasa a día tal cual
    val birth = civilFromEpochDay(birthDateMillis.floorDiv(MILLIS_PER_DAY))
    // "Hoy" depende de dónde esté el usuario: se aplica su desfase horario
    val todayEpochDay = (nowMillis + timeZone.getOffset(nowMillis)).floorDiv(MILLIS_PER_DAY)
    return daysUntilBirthday(birth.month, birth.day, todayEpochDay)
}

/** Próximo cumpleaños: cuántos días faltan y cuántos años cumplirá. */
data class UpcomingBirthday(val daysUntil: Int, val age: Int)

/**
 * Como [daysUntilBirthday], pero además devuelve la edad que cumplirá
 * la persona en ese próximo cumpleaños.
 */
fun upcomingBirthday(
    birthDateMillis: Long,
    nowMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault(),
): UpcomingBirthday {
    val birth = civilFromEpochDay(birthDateMillis.floorDiv(MILLIS_PER_DAY))
    val todayEpochDay = (nowMillis + timeZone.getOffset(nowMillis)).floorDiv(MILLIS_PER_DAY)
    return upcomingBirthday(birth, todayEpochDay)
}

internal fun upcomingBirthday(birth: CivilDate, todayEpochDay: Long): UpcomingBirthday {
    val days = daysUntilBirthday(birth.month, birth.day, todayEpochDay)
    // Año en que cae el próximo cumpleaños menos año de nacimiento
    val birthdayYear = civilFromEpochDay(todayEpochDay + days).year
    return UpcomingBirthday(daysUntil = days, age = (birthdayYear - birth.year).toInt())
}

/**
 * Núcleo del cálculo, sin relojes ni zonas horarias (fácil de testear).
 *
 * @param todayEpochDay día de hoy contado como días desde el 1-1-1970.
 */
internal fun daysUntilBirthday(birthMonth: Int, birthDay: Int, todayEpochDay: Long): Int {
    val currentYear = civilFromEpochDay(todayEpochDay).year
    var nextBirthday = birthdayEpochDay(currentYear, birthMonth, birthDay)
    if (nextBirthday < todayEpochDay) {
        // Este año ya pasó: el próximo es el año que viene
        nextBirthday = birthdayEpochDay(currentYear + 1, birthMonth, birthDay)
    }
    return (nextBirthday - todayEpochDay).toInt()
}

/** Día (desde 1-1-1970) en que se celebra el cumpleaños en [year]. */
private fun birthdayEpochDay(year: Long, month: Int, day: Int): Long {
    val celebratedDay = if (month == 2 && day == 29 && !isLeapYear(year)) 28 else day
    return epochDayFromCivil(year, month, celebratedDay)
}

internal fun isLeapYear(year: Long): Boolean =
    year % 4 == 0L && (year % 100 != 0L || year % 400 == 0L)

// ---------------------------------------------------------------------------
// Conversión entre fecha (año, mes, día) y "epoch day" (días desde 1-1-1970).
// Trabajar con un número de días hace que restar fechas sea una simple resta.
//
// No usamos java.time (LocalDate) porque necesita Android 8 (API 26) y la app
// soporta desde Android 7 (API 24). Algoritmos de Howard Hinnant, exactos para
// cualquier fecha del calendario gregoriano:
// https://howardhinnant.github.io/date_algorithms.html
// ---------------------------------------------------------------------------

internal data class CivilDate(val year: Long, val month: Int, val day: Int)

internal fun epochDayFromCivil(year: Long, month: Int, day: Int): Long {
    val y = if (month <= 2) year - 1 else year      // el año "empieza" en marzo
    val era = y.floorDiv(400L)                       // bloques de 400 años
    val yearOfEra = y - era * 400                    // [0, 399]
    val monthFromMarch = if (month > 2) month - 3 else month + 9 // mar=0 ... feb=11
    val dayOfYear = (153 * monthFromMarch + 2) / 5 + day - 1     // [0, 365]
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146_097 + dayOfEra - 719_468
}

internal fun civilFromEpochDay(epochDay: Long): CivilDate {
    val z = epochDay + 719_468
    val era = z.floorDiv(146_097L)
    val dayOfEra = z - era * 146_097                                  // [0, 146096]
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val mp = (5 * dayOfYear + 2) / 153                               // mar=0 ... feb=11
    val day = (dayOfYear - (153 * mp + 2) / 5 + 1).toInt()
    val month = (if (mp < 10) mp + 3 else mp - 9).toInt()
    val year = yearOfEra + era * 400 + if (month <= 2) 1 else 0
    return CivilDate(year, month, day)
}
