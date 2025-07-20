@file:Suppress("DEPRECATION")
package com.abhiram.flowtune.ui.component

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.LocaleList
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.abhiram.flowtune.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * LocaleManager mejorado para manejo de idiomas en aplicaciones Jetpack Compose
 */
class LocaleManager internal constructor(private val context: Context) {

    companion object {
        private const val TAG = "LocaleManager"
        private const val PREF_NAME = "locale_preferences"
        private const val PREF_LANGUAGE_KEY = "selected_language"
        private const val SYSTEM_DEFAULT = "system_default"

        // Idiomas con escritura compleja
        private val COMPLEX_SCRIPT_LANGUAGES = setOf(
            "ne", "mr", "hi", "bn", "pa", "gu", "ta", "te", "kn", "ml",
            "si", "th", "lo", "my", "ka", "am", "km",
            "zh-CN", "zh-TW", "zh-HK", "ja", "ko"
        )

        @Volatile
        private var instance: LocaleManager? = null

        fun getInstance(context: Context): LocaleManager {
            return instance ?: synchronized(this) {
                instance ?: LocaleManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val _currentLanguage = MutableStateFlow(getSelectedLanguageCode())
    val currentLanguage: StateFlow<String> = _currentLanguage

    // Cache para idiomas disponibles
    private var _availableLanguages: Map<String, String>? = null

    /**
     * Obtiene el código de idioma seleccionado por el usuario
     */
    fun getSelectedLanguageCode(): String {
        val saved = sharedPreferences.getString(PREF_LANGUAGE_KEY, SYSTEM_DEFAULT) ?: SYSTEM_DEFAULT
        return if (saved == SYSTEM_DEFAULT) getSystemLanguageCode() else saved
    }

    /**
     * Obtiene el código de idioma del sistema
     */
    private fun getSystemLanguageCode(): String {
        return try {
            val localeList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ConfigurationCompat.getLocales(Resources.getSystem().configuration)
            } else {
                LocaleListCompat.create(Locale.getDefault())
            }

            val systemLocale = if (localeList.isEmpty) Locale.getDefault() else localeList[0] ?: Locale.getDefault()
            val language = systemLocale.language
            val country = systemLocale.country

            // Manejo especial para idiomas con variantes regionales
            when {
                language == "zh" && country.isNotEmpty() -> {
                    when (country) {
                        "CN" -> "zh-CN"
                        "TW" -> "zh-TW"
                        "HK" -> "zh-HK"
                        else -> "zh-CN"
                    }
                }
                language == "pt" && country == "BR" -> "pt-BR"
                else -> language
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo idioma del sistema", e)
            "en" // Fallback seguro
        }
    }

    /**
     * Actualiza el idioma de la aplicación
     */
    fun updateLocale(languageCode: String): Boolean {
        return try {
            Log.d(TAG, "Intentando cambiar idioma a: $languageCode")

            // Determinar el código real a aplicar
            val actualLanguageCode = if (languageCode == SYSTEM_DEFAULT) {
                getSystemLanguageCode()
            } else {
                languageCode
            }

            // Guardar preferencia
            sharedPreferences.edit().putString(PREF_LANGUAGE_KEY, languageCode).apply()
            _currentLanguage.value = actualLanguageCode

            // Crear y aplicar locale
            val locale = createLocaleFromCode(actualLanguageCode)
            Log.d(TAG, "Locale creado: ${locale.language}-${locale.country}")

            // Aplicar configuración
            applyLocaleToApp(locale)

            Log.d(TAG, "Idioma actualizado exitosamente a: $actualLanguageCode")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando idioma a $languageCode", e)
            false
        }
    }

    /**
     * Aplica la configuración de idioma a la aplicación
     */
    private fun applyLocaleToApp(locale: Locale) {
        try {
            // Establecer locale por defecto
            Locale.setDefault(locale)

            // Crear nueva configuración
            val config = Configuration(context.resources.configuration)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                config.setLocales(localeList)
                config.setLocale(locale)
            } else {
                config.locale = locale
            }

            // Aplicar configuración
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            Log.d(TAG, "Configuración de idioma aplicada: ${locale.language}")
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando configuración de idioma", e)
        }
    }

    /**
     * Aplica el idioma a un contexto específico
     */
    fun applyLocaleToContext(baseContext: Context): Context {
        return try {
            val languageCode = getSelectedLanguageCode()
            val locale = createLocaleFromCode(languageCode)

            Locale.setDefault(locale)
            val config = Configuration(baseContext.resources.configuration)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                config.setLocales(localeList)
                baseContext.createConfigurationContext(config)
            } else {
                config.locale = locale
                @Suppress("DEPRECATION")
                baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
                baseContext
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando idioma al contexto", e)
            baseContext
        }
    }

    /**
     * Crea un objeto Locale desde un código de idioma
     */
    private fun createLocaleFromCode(languageCode: String): Locale {
        return try {
            when {
                languageCode == "zh-CN" -> Locale.SIMPLIFIED_CHINESE
                languageCode == "zh-TW" -> Locale.TRADITIONAL_CHINESE
                languageCode == "zh-HK" -> Locale("zh", "HK")

                languageCode in COMPLEX_SCRIPT_LANGUAGES -> {
                    if (languageCode.contains("-")) {
                        val (language, country) = languageCode.split("-")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Locale.Builder()
                                .setLanguage(language)
                                .setRegion(country)
                                .setScript(getScriptForLanguage(languageCode))
                                .build()
                        } else {
                            Locale(language, country)
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Locale.Builder()
                                .setLanguage(languageCode)
                                .setScript(getScriptForLanguage(languageCode))
                                .build()
                        } else {
                            Locale(languageCode)
                        }
                    }
                }

                languageCode.contains("-") -> {
                    val parts = languageCode.split("-", limit = 2)
                    if (parts.size >= 2) {
                        Locale(parts[0], parts[1])
                    } else {
                        Locale(parts[0])
                    }
                }

                else -> Locale(languageCode)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creando Locale para: $languageCode", e)
            Locale(languageCode) // Fallback simple
        }
    }

    /**
     * Obtiene el script apropiado para idiomas complejos
     */
    private fun getScriptForLanguage(languageCode: String): String {
        return when (languageCode.substringBefore("-")) {
            "hi", "mr" -> "Deva" // Devanagari
            "bn" -> "Beng" // Bengali
            "pa" -> "Guru" // Gurmukhi
            "gu" -> "Gujr" // Gujarati
            "ta" -> "Taml" // Tamil
            "te" -> "Telu" // Telugu
            "kn" -> "Knda" // Kannada
            "ml" -> "Mlym" // Malayalam
            "si" -> "Sinh" // Sinhala
            "th" -> "Thai" // Thai
            "ka" -> "Geor" // Georgian
            "am" -> "Ethi" // Ethiopic
            "km" -> "Khmr" // Khmer
            "ne" -> "Deva" // Nepali uses Devanagari
            "lo" -> "Laoo" // Lao
            "my" -> "Mymr" // Myanmar/Burmese
            else -> ""
        }
    }

    /**
     * Obtiene los idiomas disponibles (manteniendo todos los originales)
     */
    fun getAvailableLanguages(): Map<String, String> {
        _availableLanguages?.let { return it }

        val languages = mutableMapOf<String, String>()

        try {
            // Agregar opción de sistema por defecto
            languages[SYSTEM_DEFAULT] = "Sistema (${getLanguageDisplayName(getSystemLanguageCode())})"

            // Todos los idiomas originales
            val allLanguages = listOf(
                "de", "af", "ar", "be", "bn", "ca", "cs", "da", "de", "el", "en", "es",
                "fa", "fr", "hu", "id", "it", "iw", "ja", "ko", "ml", "ne", "nl", "no",
                "or", "pa", "pl", "pt-BR", "ru", "ro", "sr", "sv", "hi", "tr", "uk",
                "vi", "zh", "zh-CN", "zh-TW", "zh-HK", "gu", "ta", "te", "kn",
                "si", "th", "lo", "my", "ka", "am", "km", "mr"
            )

            // Agregar todos los idiomas con sus nombres de display
            for (langCode in allLanguages.distinct()) {
                languages[langCode] = getLanguageDisplayName(langCode)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo idiomas disponibles", e)
            // Fallback mínimo
            languages["en"] = "English"
            languages["es"] = "Español"
        }

        _availableLanguages = languages
        return languages
    }

    /**
     * Obtiene el nombre de display de un idioma
     */
    private fun getLanguageDisplayName(languageCode: String): String {
        return try {
            val locale = createLocaleFromCode(languageCode)
            val nativeName = locale.getDisplayLanguage(locale)
            val englishName = locale.getDisplayLanguage(Locale.ENGLISH)

            // Formato: "English Name (Native Name)" o solo "English Name" si son iguales
            if (nativeName != englishName && nativeName.isNotEmpty()) {
                "$englishName ($nativeName)"
            } else {
                englishName
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo nombre para $languageCode", e)
            languageCode
        }
    }

    /**
     * Reinicia la aplicación de forma segura
     */
    fun restartApp(context: Context) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

                Handler(Looper.getMainLooper()).postDelayed({
                    context.startActivity(it)
                    if (context is Activity) {
                        context.finish()
                    }
                }, 300)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reiniciando aplicación", e)
        }
    }
}

/**
 * Composable para manejar el estado del idioma
 */
@Composable
fun rememberLanguageState(): Pair<String, (String) -> Unit> {
    val context = LocalContext.current
    val localeManager = remember { LocaleManager.getInstance(context) }

    val currentLanguage by localeManager.currentLanguage.collectAsState()
    val availableLanguages = remember { localeManager.getAvailableLanguages() }

    val changeLanguage: (String) -> Unit = { newLanguage ->
        Log.d("LanguageState", "Cambiando idioma a: $newLanguage")

        if (localeManager.updateLocale(newLanguage)) {
            val displayName = availableLanguages[newLanguage] ?: newLanguage

            Toast.makeText(
                context,
                "Cambiando idioma a $displayName...",
                Toast.LENGTH_SHORT
            ).show()

            // Reiniciar después de un breve delay
            Handler(Looper.getMainLooper()).postDelayed({
                localeManager.restartApp(context)
            }, 1000)
        } else {
            Toast.makeText(
                context,
                "Error al cambiar idioma",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    return currentLanguage to changeLanguage
}

/**
 * Composable para la preferencia de idioma
 */
@Composable
fun LanguagePreference() {
    val context = LocalContext.current
    val localeManager = remember { LocaleManager.getInstance(context) }

    // Inicializar idiomas disponibles
    LaunchedEffect(Unit) {
        localeManager.getAvailableLanguages()
    }

    val availableLanguages = remember { localeManager.getAvailableLanguages() }
    val (selectedLanguage, changeLanguage) = rememberLanguageState()

    ListPreference(
        title = { Text("Idioma de la aplicación") },
        icon = { Icon(painterResource(R.drawable.translate), null) },
        selectedValue = selectedLanguage,
        values = availableLanguages.keys.toList(),
        valueText = { availableLanguages[it] ?: "Desconocido" },
        onValueSelected = changeLanguage
    )
}

/**
 * Application class base que aplica el idioma guardado
 */
abstract class LocaleAwareApplication : android.app.Application() {
    override fun attachBaseContext(base: Context) {
        val localeManager = LocaleManager.getInstance(base)
        val updatedContext = localeManager.applyLocaleToContext(base)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate() {
        super.onCreate()
        LocaleManager.getInstance(this)
    }
}