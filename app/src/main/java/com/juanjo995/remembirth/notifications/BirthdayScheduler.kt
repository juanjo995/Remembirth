package com.juanjo995.remembirth.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Programa cuándo se comprueban los cumpleaños. */
object BirthdayScheduler {

    /** Hora (aprox.) de la comprobación diaria. */
    private const val CHECK_HOUR = 9

    private const val DAILY_WORK = "birthday_daily_check"
    private const val NOW_WORK = "birthday_check_now"

    /**
     * Comprobación diaria a las ~9:00. Se llama al abrir la app; si ya estaba
     * programada no se toca (KEEP), así no se reinicia el horario cada vez.
     * WorkManager la mantiene aunque se cierre la app o se reinicie el móvil.
     */
    fun scheduleDaily(context: Context) {
        val request = PeriodicWorkRequestBuilder<BirthdayWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNextCheck(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(DAILY_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /**
     * Comprobación inmediata.
     *
     * @param personId si se indica, solo se revisa esa persona (tras añadirla o editarla),
     *   para no volver a mostrar las notificaciones de los demás que el usuario ya descartó.
     */
    fun checkNow(context: Context, personId: Long? = null) {
        val request = OneTimeWorkRequestBuilder<BirthdayWorker>()
            .setInputData(workDataOf(BirthdayWorker.KEY_PERSON_ID to (personId ?: BirthdayWorker.ALL)))
            .build()
        // Nombre único por persona: si se añaden dos seguidas, no se pisa una a la otra
        WorkManager.getInstance(context)
            .enqueueUniqueWork("${NOW_WORK}_${personId ?: "all"}", ExistingWorkPolicy.REPLACE, request)
    }

    /** Milisegundos que faltan hasta las próximas [CHECK_HOUR]:00. */
    private fun millisUntilNextCheck(): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, CHECK_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }
}
