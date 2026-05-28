package `in`.vedicpanchang.app.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

/**
 * DataStore-backed preferences replacing SharedPreferences from Flutter's settings_provider.dart.
 * Keys mirror the Dart preference keys for consistency.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LOCALE = stringPreferencesKey("locale")                         // "en" | "hi" | "sa"
        val THEME_MODE = stringPreferencesKey("theme_mode")                 // "light" | "dark" | "system"
        val NOTIF_EKADASHI = booleanPreferencesKey("notif_ekadashi")
        val NOTIF_PURNIMA = booleanPreferencesKey("notif_purnima")
        val NOTIF_AMAVASYA = booleanPreferencesKey("notif_amavasya")
        val NOTIF_FESTIVALS = booleanPreferencesKey("notif_festivals")
        val NOTIF_BRAHMA = booleanPreferencesKey("notif_brahma_muhurta")
        val NOTIF_DAYS3 = booleanPreferencesKey("notif_days3")
        val NOTIF_DAY1 = booleanPreferencesKey("notif_day1")
        val NOTIF_SAME_DAY = booleanPreferencesKey("notif_same_day")
        val CACHED_LAT = doublePreferencesKey("cached_lat")
        val CACHED_LON = doublePreferencesKey("cached_lon")
        val CACHED_CITY = stringPreferencesKey("cached_city")
        val CACHED_COUNTRY = stringPreferencesKey("cached_country")
    }

    // ── Locale ────────────────────────────────────────────────────────────────

    val locale: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.LOCALE] ?: "en" }

    suspend fun setLocale(locale: String) {
        context.dataStore.edit { it[Keys.LOCALE] = locale }
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    val themeMode: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.THEME_MODE] ?: "system" }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    // ── Notification settings ─────────────────────────────────────────────────

    data class NotificationSettings(
        val ekadashi: Boolean = true,
        val purnima: Boolean = true,
        val amavasya: Boolean = true,
        val festivals: Boolean = true,
        val brahmaMuhurta: Boolean = false,
        val days3: Boolean = true,
        val day1: Boolean = true,
        val sameDay: Boolean = true
    )

    val notificationSettings: Flow<NotificationSettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            NotificationSettings(
                ekadashi = prefs[Keys.NOTIF_EKADASHI] ?: true,
                purnima = prefs[Keys.NOTIF_PURNIMA] ?: true,
                amavasya = prefs[Keys.NOTIF_AMAVASYA] ?: true,
                festivals = prefs[Keys.NOTIF_FESTIVALS] ?: true,
                brahmaMuhurta = prefs[Keys.NOTIF_BRAHMA] ?: false,
                days3 = prefs[Keys.NOTIF_DAYS3] ?: true,
                day1 = prefs[Keys.NOTIF_DAY1] ?: true,
                sameDay = prefs[Keys.NOTIF_SAME_DAY] ?: true
            )
        }

    suspend fun setNotificationSettings(settings: NotificationSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIF_EKADASHI] = settings.ekadashi
            prefs[Keys.NOTIF_PURNIMA] = settings.purnima
            prefs[Keys.NOTIF_AMAVASYA] = settings.amavasya
            prefs[Keys.NOTIF_FESTIVALS] = settings.festivals
            prefs[Keys.NOTIF_BRAHMA] = settings.brahmaMuhurta
            prefs[Keys.NOTIF_DAYS3] = settings.days3
            prefs[Keys.NOTIF_DAY1] = settings.day1
            prefs[Keys.NOTIF_SAME_DAY] = settings.sameDay
        }
    }

    // ── Cached location ───────────────────────────────────────────────────────

    data class CachedLocation(
        val latitude: Double,
        val longitude: Double,
        val city: String,
        val country: String
    )

    val cachedLocation: Flow<CachedLocation?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val lat = prefs[Keys.CACHED_LAT] ?: return@map null
            val lon = prefs[Keys.CACHED_LON] ?: return@map null
            CachedLocation(lat, lon, prefs[Keys.CACHED_CITY] ?: "", prefs[Keys.CACHED_COUNTRY] ?: "")
        }

    suspend fun saveCachedLocation(location: CachedLocation) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CACHED_LAT] = location.latitude
            prefs[Keys.CACHED_LON] = location.longitude
            prefs[Keys.CACHED_CITY] = location.city
            prefs[Keys.CACHED_COUNTRY] = location.country
        }
    }
}
