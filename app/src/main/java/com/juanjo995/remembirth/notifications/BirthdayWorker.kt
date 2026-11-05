package com.juanjo995.remembirth.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.juanjo995.remembirth.data.RemembirthDatabase

/**
 * Tarea en segundo plano: lee las personas y lanza las notificaciones.
 * La ejecuta WorkManager (una vez al día y tras añadir/editar), aunque la app esté cerrada.
 */
class BirthdayWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val all = RemembirthDatabase.getInstance(applicationContext).personDao().getAllOnce()
        // ¿Revisar a todos (comprobación diaria) o solo a una persona (tras añadir/editar)?
        val personId = inputData.getLong(KEY_PERSON_ID, ALL)
        val persons = if (personId == ALL) all else all.filter { it.id == personId }
        BirthdayNotifier.notifyUpcoming(applicationContext, persons)
        return Result.success()
    }

    companion object {
        const val KEY_PERSON_ID = "person_id"
        const val ALL = -1L
    }
}
