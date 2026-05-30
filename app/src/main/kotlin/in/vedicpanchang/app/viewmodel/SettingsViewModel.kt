package `in`.vedicpanchang.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.vedicpanchang.app.data.datasource.AppPreferences
import `in`.vedicpanchang.app.l10n.AppStrings
import `in`.vedicpanchang.app.l10n.HoroscopeLocalizer
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.service.NotificationService
import `in`.vedicpanchang.app.service.NotificationScheduler
import `in`.vedicpanchang.app.service.LocationService
import `in`.vedicpanchang.app.data.datasource.AppPreferences.NotificationSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages locale, theme, notification settings, and derived localizer instances.
 * Equivalent of settings_provider.dart: LocaleNotifier + ThemeModeNotifier +
 * NotificationSettingsNotifier + stringsProvider + localizerProvider + appVersionProvider.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: AppPreferences,
    private val notificationService: NotificationService,
    private val notificationScheduler: NotificationScheduler,
    private val locationService: LocationService
) : ViewModel() {

    // ── Locale ────────────────────────────────────────────────────────────────

    val locale: StateFlow<String> = preferences.locale
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppStrings.DEFAULT_LOCALE)

    val strings: StateFlow<Map<String, String>> = preferences.locale
        .map { AppStrings.of(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppStrings.of(AppStrings.DEFAULT_LOCALE))

    val panchangLocalizer: StateFlow<PanchangLocalizer> = preferences.locale
        .map { PanchangLocalizer(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PanchangLocalizer(AppStrings.DEFAULT_LOCALE))

    val horoscopeLocalizer: StateFlow<HoroscopeLocalizer> = preferences.locale
        .map { HoroscopeLocalizer(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HoroscopeLocalizer(AppStrings.DEFAULT_LOCALE))

    fun setLocale(locale: String) {
        viewModelScope.launch { preferences.setLocale(locale) }
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    val themeMode: StateFlow<String> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    // ── Notification settings ─────────────────────────────────────────────────

    val notificationSettings: StateFlow<NotificationSettings> = preferences.notificationSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, NotificationSettings())

    private fun updateSettings(transform: (NotificationSettings) -> NotificationSettings) {
        viewModelScope.launch {
            val current = preferences.notificationSettings.first()
            preferences.setNotificationSettings(transform(current))
            rescheduleNotifications()
        }
    }

    fun toggleEkadashi(enabled: Boolean) = updateSettings { it.copy(ekadashi = enabled) }
    fun togglePurnima(enabled: Boolean)  = updateSettings { it.copy(purnima = enabled) }
    fun toggleAmavasya(enabled: Boolean) = updateSettings { it.copy(amavasya = enabled) }
    fun toggleFestivals(enabled: Boolean)= updateSettings { it.copy(festivals = enabled) }
    fun toggle3Days(enabled: Boolean)    = updateSettings { it.copy(days3 = enabled) }
    fun toggle1Day(enabled: Boolean)     = updateSettings { it.copy(day1 = enabled) }
    fun toggleSameDay(enabled: Boolean)  = updateSettings { it.copy(sameDay = enabled) }

    private fun rescheduleNotifications() {
        viewModelScope.launch(Dispatchers.Default) {
            val cachedLoc = preferences.cachedLocation.first() ?: return@launch
            val settings = preferences.notificationSettings.first()
            val locale = preferences.locale.first()
            runCatching {
                notificationScheduler.reschedule(
                    lat = cachedLoc.latitude,
                    lon = cachedLoc.longitude,
                    locationName = cachedLoc.city,
                    settings = settings,
                    locale = locale
                )
            }
        }
    }

    // ── App version ───────────────────────────────────────────────────────────

    val appVersion: String by lazy {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) { "1.0.0" }
    }
}
