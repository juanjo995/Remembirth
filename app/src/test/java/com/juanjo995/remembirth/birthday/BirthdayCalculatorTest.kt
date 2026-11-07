package com.juanjo995.remembirth.birthday

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

/**
 * Tests de [daysUntilBirthday]. Se ejecutan en el PC (no hace falta móvil):
 * clic derecho en este archivo > Run 'BirthdayCalculatorTest'.
 */
class BirthdayCalculatorTest {

    private fun day(y: Long, m: Int, d: Int) = epochDayFromCivil(y, m, d)

    private fun until(birthMonth: Int, birthDay: Int, today: Long) =
        daysUntilBirthday(birthMonth, birthDay, today)

    @Test
    fun cumpleanosHoy_devuelveCero() {
        assertEquals(0, until(7, 15, day(2026, 7, 15)))
    }

    @Test
    fun cumpleanosManana_devuelveUno() {
        assertEquals(1, until(7, 15, day(2026, 7, 14)))
    }

    @Test
    fun cumpleanosAyer_faltaCasiUnAno() {
        assertEquals(364, until(7, 15, day(2026, 7, 16))) // 2027 no es bisiesto
        assertEquals(365, until(7, 15, day(2027, 7, 16))) // pasa por el 29-2-2028
    }

    @Test
    fun cambioDeAno() {
        assertEquals(1, until(1, 1, day(2026, 12, 31)))
        assertEquals(0, until(12, 31, day(2026, 12, 31)))
    }

    @Test
    fun nacido29Febrero_enAnoBisiesto_loCelebraEl29() {
        assertEquals(0, until(2, 29, day(2028, 2, 29)))
        assertEquals(1, until(2, 29, day(2028, 2, 28)))
    }

    @Test
    fun nacido29Febrero_enAnoNoBisiesto_loCelebraEl28() {
        assertEquals(0, until(2, 29, day(2027, 2, 28)))
        assertEquals(1, until(2, 29, day(2027, 2, 27)))
    }

    @Test
    fun nacido29Febrero_justoDespues_esperaAlAnoSiguiente() {
        // 1-3-2027 -> 29-2-2028
        assertEquals(365, until(2, 29, day(2027, 3, 1)))
        // 1-3-2028 -> 28-2-2029
        assertEquals(364, until(2, 29, day(2028, 3, 1)))
    }

    @Test
    fun anos1900y2000_reglaDeBisiestos() {
        assertEquals(false, isLeapYear(1900))
        assertEquals(true, isLeapYear(2000))
        assertEquals(false, isLeapYear(2100))
    }

    @Test
    fun conversionDeFechas_idaYVuelta() {
        assertEquals(0L, day(1970, 1, 1))
        assertEquals(-1L, day(1969, 12, 31))
        var d = day(1900, 1, 1)
        val end = day(2100, 12, 31)
        while (d <= end) {
            val c = civilFromEpochDay(d)
            assertEquals(d, day(c.year, c.month, c.day))
            d++
        }
    }

    @Test
    fun funcionPublica_conMilisYZonaHoraria() {
        val madrid = TimeZone.getTimeZone("Europe/Madrid")
        // Nacido el 6-11-1995 (medianoche UTC, como lo guarda el DatePicker)
        val birth = day(1995, 11, 6) * 86_400_000L
        // 5-11-2026 a las 23:30 en Madrid (22:30 UTC): mañana es el cumpleaños
        val now = day(2026, 11, 5) * 86_400_000L + (22 * 60 + 30) * 60_000L
        assertEquals(1, daysUntilBirthday(birth, now, madrid))
        // 6-11-2026 a las 00:30 en Madrid (5-11 a las 23:30 UTC): ya es hoy
        val afterMidnight = now + 60 * 60_000L
        assertEquals(0, daysUntilBirthday(birth, afterMidnight, madrid))
    }

    @Test
    fun edad_cumpleEsteAnoOElSiguiente() {
        val birth = CivilDate(1995, 11, 6)
        // Antes del cumpleaños de 2026: cumplirá 31 este año
        assertEquals(UpcomingBirthday(1, 31), upcomingBirthday(birth, day(2026, 11, 5)))
        // Hoy es el cumpleaños
        assertEquals(UpcomingBirthday(0, 31), upcomingBirthday(birth, day(2026, 11, 6)))
        // Ya pasó: el próximo es en 2027, cumplirá 32
        assertEquals(UpcomingBirthday(364, 32), upcomingBirthday(birth, day(2026, 11, 7)))
    }

    @Test
    fun edad_nacido29Febrero() {
        val birth = CivilDate(2000, 2, 29)
        // 28-2-2027 (no bisiesto): lo celebra hoy y cumple 27
        assertEquals(UpcomingBirthday(0, 27), upcomingBirthday(birth, day(2027, 2, 28)))
    }
}
