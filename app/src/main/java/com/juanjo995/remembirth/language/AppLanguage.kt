package com.juanjo995.remembirth.language

import android.content.Context
import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.settings.settingsPrefs
import java.util.Locale

/**
 * Idiomas soportados por la app, en el orden en que aparecen en el desplegable.
 *
 * - [tag]: código de idioma BCP-47 que entiende Android ("en", "es", "zh-CN"...).
 * - [displayName]: nombre del idioma escrito en ese mismo idioma, para que
 *   cualquier usuario reconozca el suyo aunque la app esté en otro.
 * - [flagRes]: bandera (circle-flags) en res/drawable.
 *
 * Para añadir un idioma: una entrada aquí, su carpeta res/values-XX/strings.xml,
 * su bandera en res/drawable y una línea en res/xml/locales_config.xml.
 */
enum class AppLanguage(
    val tag: String,
    val displayName: String,
    @param:DrawableRes val flagRes: Int,
) {
    INDONESIAN("id", "Bahasa Indonesia", R.drawable.flag_id),
    CATALAN("ca", "Català", R.drawable.flag_es_ct),
    CZECH("cs", "Čeština", R.drawable.flag_cz),
    DANISH("da", "Dansk", R.drawable.flag_dk),
    GERMAN("de", "Deutsch", R.drawable.flag_de),
    ENGLISH("en", "English", R.drawable.flag_gb),
    SPANISH("es", "Español", R.drawable.flag_es),
    FRENCH("fr", "Français", R.drawable.flag_fr),
    GALICIAN("gl", "Galego", R.drawable.flag_es_ga),
    ITALIAN("it", "Italiano", R.drawable.flag_it),
    HUNGARIAN("hu", "Magyar", R.drawable.flag_hu),
    DUTCH("nl", "Nederlands", R.drawable.flag_nl),
    NORWEGIAN("nb", "Norsk bokmål", R.drawable.flag_no),
    POLISH("pl", "Polski", R.drawable.flag_pl),
    PORTUGUESE("pt", "Português", R.drawable.flag_pt),
    ROMANIAN("ro", "Română", R.drawable.flag_ro),
    FINNISH("fi", "Suomi", R.drawable.flag_fi),
    SWEDISH("sv", "Svenska", R.drawable.flag_se),
    VIETNAMESE("vi", "Tiếng Việt", R.drawable.flag_vn),
    TURKISH("tr", "Türkçe", R.drawable.flag_tr),
    GREEK("el", "Ελληνικά", R.drawable.flag_gr),
    RUSSIAN("ru", "Русский", R.drawable.flag_ru),
    UKRAINIAN("uk", "Українська", R.drawable.flag_ua),
    HEBREW("he", "עברית", R.drawable.flag_il),
    ARABIC("ar", "العربية", R.drawable.flag_sa),
    HINDI("hi", "हिन्दी", R.drawable.flag_in),
    THAI("th", "ไทย", R.drawable.flag_th),
    KOREAN("ko", "한국어", R.drawable.flag_kr),
    JAPANESE("ja", "日本語", R.drawable.flag_jp),
    CHINESE_SIMPLIFIED("zh-CN", "简体中文", R.drawable.flag_cn),
    CHINESE_TRADITIONAL("zh-TW", "繁體中文", R.drawable.flag_tw);

    companion object {
        private const val KEY_LANGUAGE = "language"

        /**
         * Cambia el idioma de la app: lo aplica (la pantalla se recrea traducida)
         * y lo guarda para las notificaciones, que se crean en segundo plano.
         */
        fun apply(context: Context, language: AppLanguage) {
            save(context, language)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        }

        /** Solo guarda el idioma (para las notificaciones), sin aplicarlo. */
        fun save(context: Context, language: AppLanguage) {
            context.settingsPrefs().edit { putString(KEY_LANGUAGE, language.tag) }
        }

        /** Código del idioma guardado ("en", "es"...), o null si nunca se eligió. */
        fun savedTag(context: Context): String? =
            context.settingsPrefs().getString(KEY_LANGUAGE, null)

        /** Idioma por defecto: el del sistema si lo soportamos, si no inglés. */
        fun fromSystem(): AppLanguage {
            // Resources.getSystem(): idioma del móvil, sin el idioma propio de la app
            val systemLocale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
                ?: Locale.getDefault()
            return fromLocale(systemLocale) ?: ENGLISH
        }

        /** Idioma elegido para la app; si no hay ninguno, el del sistema. */
        fun current(): AppLanguage =
            AppCompatDelegate.getApplicationLocales()[0]?.let(::fromLocale) ?: fromSystem()

        /** El idioma de la app que corresponde a un Locale (p. ej. "es-MX" -> Español). */
        fun fromLocale(locale: Locale): AppLanguage? {
            val language = normalize(locale.language)
            if (language == "zh") {
                // Chino: tradicional en Taiwán, Hong Kong y Macao; simplificado en el resto
                val traditional = locale.script == "Hant" || locale.country in setOf("TW", "HK", "MO")
                return if (traditional) CHINESE_TRADITIONAL else CHINESE_SIMPLIFIED
            }
            return entries.firstOrNull { normalize(Locale.forLanguageTag(it.tag).language) == language }
        }

        /**
         * Java y Android usan a veces códigos antiguos para algunos idiomas
         * ("iw" = hebreo, "in" = indonesio, "no" = noruego). Los unificamos.
         */
        private fun normalize(language: String): String = when (language) {
            "iw" -> "he"
            "in" -> "id"
            "no" -> "nb"
            else -> language
        }
    }
}
