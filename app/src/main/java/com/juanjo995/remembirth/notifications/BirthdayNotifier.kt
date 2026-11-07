package com.juanjo995.remembirth.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.juanjo995.remembirth.MainActivity
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.birthday.birthdayHtml
import com.juanjo995.remembirth.birthday.upcomingBirthday
import com.juanjo995.remembirth.data.Person
import com.juanjo995.remembirth.language.AppLanguage
import java.util.Locale

/** Muestra y quita las notificaciones de cumpleaños. */
object BirthdayNotifier {

    /** Se avisa cuando faltan estos días o menos. */
    const val DAYS_BEFORE = 10

    private const val CHANNEL_ID = "upcoming_birthdays"

    /**
     * Revisa a todas las personas: notifica a las que les quedan [DAYS_BEFORE] días
     * o menos, y quita la notificación de las demás (p. ej. si el cumpleaños ya pasó).
     */
    fun notifyUpcoming(context: Context, persons: List<Person>) {
        val resources = localizedResources(context)
        createChannel(context, resources)

        persons.forEach { person ->
            val birthday = upcomingBirthday(person.birthDateMillis)
            if (birthday.daysUntil <= DAYS_BEFORE) {
                // Exactamente la misma frase que en la lista, con negrita y colores
                val html = birthdayHtml(resources, person.name, birthday, isSystemDark(context))
                show(context, person, HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY))
            } else {
                cancel(context, person)
            }
        }
    }

    /** Quita la notificación de una persona (al eliminarla, por ejemplo). */
    fun cancel(context: Context, person: Person) {
        NotificationManagerCompat.from(context).cancel(notificationId(person))
    }

    // SuppressLint: el permiso se comprueba justo antes, pero Lint no siempre lo detecta
    @SuppressLint("MissingPermission")
    private fun show(context: Context, person: Person, text: CharSequence) {
        if (!canNotify(context)) return

        // Al pulsar la notificación se abre la app
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text)) // texto completo si es largo
            .setPriority(NotificationCompat.PRIORITY_HIGH) // pop-up en Android 7 (en 8+ lo decide el canal)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openApp)
            .setAutoCancel(true) // desaparece al pulsarla
            .build()

        // Mismo id para la misma persona: la notificación de hoy sustituye a la de ayer
        NotificationManagerCompat.from(context).notify(notificationId(person), notification)
    }

    /** ¿Tenemos permiso para notificar? (En Android 13+ el usuario tiene que concederlo.) */
    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Canal de notificaciones (Android 8+). Importancia ALTA = aparecen como pop-up.
     * Crearlo varias veces no pasa nada; así además se actualiza el nombre si cambia el idioma.
     */
    private fun createChannel(context: Context, resources: Resources) {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(resources.getString(R.string.notification_channel_name))
            .setDescription(resources.getString(R.string.notification_channel_description))
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    private fun notificationId(person: Person): Int = person.id.toInt()

    /**
     * Recursos en el idioma elegido en la app. En segundo plano no hay Activity,
     * así que se usa el idioma guardado en los ajustes.
     */
    private fun localizedResources(context: Context): Resources {
        val tag = AppLanguage.savedTag(context) ?: return context.resources
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(tag))
        return context.createConfigurationContext(config).resources
    }

    /** Las notificaciones siguen el tema del sistema (no el de la app). */
    private fun isSystemDark(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
}
