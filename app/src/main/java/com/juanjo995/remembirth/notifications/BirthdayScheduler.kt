package com.juanjo995.remembirth.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

/**
 * Comprobaciones inmediatas con WorkManager (tras añadir/editar una persona).
 * La comprobación diaria de las 9:00 la hace [DailyCheck] con una alarma.
 */
object BirthdayScheduler {

    private const val NOW_WORK = "birthday_check_now"

    /** Nombre de la antigua tarea diaria de WorkManager (versiones anteriores de la app). */
    private const val OLD_DAILY_WORK = "birthday_daily_check"

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

    /**
     * Cancela la tarea diaria de WorkManager que usaban las versiones anteriores,
     * para que no se dupliquen los avisos con la nueva alarma.
     */
    fun cancelOldDailyWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(OLD_DAILY_WORK)
    }
}
