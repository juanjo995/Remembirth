package com.juanjo995.remembirth.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.edit
import com.juanjo995.remembirth.data.RemembirthDatabase
import com.juanjo995.remembirth.settings.settingsPrefs
import java.util.Calendar

/**
 * Comprobación diaria de cumpleaños a las [CHECK_HOUR]:00.
 *
 * Usa una alarma exacta (AlarmManager) en lugar de WorkManager: WorkManager está
 * pensado para trabajo que puede esperar y Android lo retrasa horas cuando el móvil
 * está en reposo. Una alarma exacta "AllowWhileIdle" despierta al móvil a su hora.
 *
 * Las alarmas se borran al reiniciar el móvil, así que [DailyCheckReceiver] las vuelve
 * a programar. Si el móvil estaba apagado a esa hora, [runIfDue] lanza el aviso
 * en cuanto puede (al encender el móvil o abrir la app), una sola vez al día.
 */
object DailyCheck {

    /** Hora de la comprobación diaria. */
    const val CHECK_HOUR = 9

    const val ACTION_DAILY_CHECK = "com.juanjo995.remembirth.action.DAILY_CHECK"

    private const val KEY_LAST_RUN_DAY = "daily_check_last_run_day"

    /** Programa la alarma para las próximas [CHECK_HOUR]:00 (hoy o mañana). */
    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, DailyCheckReceiver::class.java).setAction(ACTION_DAILY_CHECK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val triggerAt = nextCheckTime()

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        try {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                // Sin permiso de alarma exacta: aproximada, pero también funciona en reposo
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: SecurityException) {
            // El permiso se retiró justo ahora: alarma aproximada
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    /**
     * Si ya son las [CHECK_HOUR]:00 o más y hoy todavía no se ha avisado, lanza
     * las notificaciones. Se llama desde la alarma, al encender el móvil y al abrir la app,
     * así que nunca se pierde el aviso de un día ni se repite.
     */
    suspend fun runIfDue(context: Context) {
        val now = Calendar.getInstance()
        val today = dayKey(now)
        if (now.get(Calendar.HOUR_OF_DAY) < CHECK_HOUR) return
        if (context.settingsPrefs().getInt(KEY_LAST_RUN_DAY, 0) == today) return

        val persons = RemembirthDatabase.getInstance(context).personDao().getAllOnce()
        BirthdayNotifier.notifyUpcoming(context, persons)
        context.settingsPrefs().edit { putInt(KEY_LAST_RUN_DAY, today) }
    }

    /** Instante de las próximas [CHECK_HOUR]:00. */
    private fun nextCheckTime(): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, CHECK_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return next.timeInMillis
    }

    /** Identificador del día (p. ej. 20261106) en la zona horaria del móvil. */
    private fun dayKey(calendar: Calendar): Int =
        calendar.get(Calendar.YEAR) * 10_000 +
            (calendar.get(Calendar.MONTH) + 1) * 100 +
            calendar.get(Calendar.DAY_OF_MONTH)
}
