package com.juanjo995.remembirth.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Recibe la alarma diaria y algunos avisos del sistema:
 * - la alarma de las 9:00 ([DailyCheck.ACTION_DAILY_CHECK]);
 * - el móvil se ha encendido (las alarmas se borran al apagarlo);
 * - la app se ha actualizado;
 * - ha cambiado la hora o la zona horaria.
 *
 * En todos los casos: lanza el aviso de hoy si toca y programa la próxima alarma.
 */
class DailyCheckReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        // goAsync: permite terminar el trabajo (leer la base de datos) fuera del hilo principal
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DailyCheck.runIfDue(appContext)
                DailyCheck.schedule(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
