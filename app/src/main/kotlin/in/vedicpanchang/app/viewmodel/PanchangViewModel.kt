package `in`.vedicpanchang.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.service.LocationData
import `in`.vedicpanchang.app.service.LocationService
import `in`.vedicpanchang.app.service.NotificationScheduler
import `in`.vedicpanchang.app.service.PanchangService
import `in`.vedicpanchang.app.service.WidgetService
import `in`.vedicpanchang.app.data.datasource.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface PanchangUiState {
    data object Loading : PanchangUiState
    data class Success(val panchang: PanchangModel) : PanchangUiState
    data class Error(val message: String) : PanchangUiState
}

sealed interface LocationUiState {
    data object Loading : LocationUiState
    data class Success(val location: LocationData) : LocationUiState
    data class Error(val message: String) : LocationUiState
}

data class PanchangScreenState(
    val location: LocationUiState = LocationUiState.Loading,
    val todayPanchang: PanchangUiState = PanchangUiState.Loading,
    val livePanchang: PanchangUiState = PanchangUiState.Loading,
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val selectedPanchang: PanchangUiState = PanchangUiState.Loading,
    val liveNow: kotlin.time.Instant = Clock.System.now()
)

/**
 * Manages all Panchang + location state.
 * Equivalent of panchang_provider.dart + location_provider + selectedDateProvider.
 */
@HiltViewModel
class PanchangViewModel @Inject constructor(
    private val panchangService: PanchangService,
    private val locationService: LocationService,
    private val widgetService: WidgetService,
    private val notificationScheduler: NotificationScheduler,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(PanchangScreenState())
    val state: StateFlow<PanchangScreenState> = _state.asStateFlow()

    private var liveTickJob: Job? = null
    private var lastWidgetSyncDate: LocalDate? = null
    private var lastNotifSyncDate: LocalDate? = null

    init {
        loadLocation()
        startLiveTick()
    }

    // ── Location ──────────────────────────────────────────────────────────────

    fun loadLocation() {
        viewModelScope.launch {
            _state.update { it.copy(location = LocationUiState.Loading) }
            try {
                // Read cache first (instant)
                val cached = preferences.cachedLocation.first()
                val cachedLocation = cached?.let {
                    LocationData(it.latitude, it.longitude, it.city, it.country)
                }
                if (cachedLocation != null) {
                    _state.update { it.copy(location = LocationUiState.Success(cachedLocation)) }
                    loadTodayPanchang(cachedLocation)
                    loadSelectedPanchang(cachedLocation)
                }

                // Always attempt a fresh location; update if it differs
                val currentLocation = locationService.getCurrentLocationOrNull()
                if (currentLocation != null) {
                    val shouldUpdate = cachedLocation == null || !isSameLocation(cachedLocation, currentLocation)
                    if (shouldUpdate) {
                        _state.update { it.copy(location = LocationUiState.Success(currentLocation)) }
                        loadTodayPanchang(currentLocation)
                        loadSelectedPanchang(currentLocation)
                    }
                } else if (cachedLocation == null) {
                    val fallback = LocationData.DEFAULT
                    _state.update { it.copy(location = LocationUiState.Success(fallback)) }
                    loadTodayPanchang(fallback)
                    loadSelectedPanchang(fallback)
                }
            } catch (e: Exception) {
                val fallback = LocationData.DEFAULT
                _state.update { it.copy(location = LocationUiState.Error(e.message ?: "Location failed")) }
                loadTodayPanchang(fallback)
                loadSelectedPanchang(fallback)
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val previous = _state.value.location
            _state.update { it.copy(location = LocationUiState.Loading) }
            try {
                val location = locationService.getCurrentLocationOrNull()
                if (location != null) {
                    _state.update { it.copy(location = LocationUiState.Success(location)) }
                    loadTodayPanchang(location)
                    loadSelectedPanchang(location)
                } else {
                    val nextState = if (previous is LocationUiState.Success) previous
                                    else LocationUiState.Error("Location failed")
                    _state.update { it.copy(location = nextState) }
                }
            } catch (e: Exception) {
                val nextState = if (previous is LocationUiState.Success) previous
                                else LocationUiState.Error(e.message ?: "Location failed")
                _state.update { it.copy(location = nextState) }
            }
        }
    }

    // ── Today's Panchang ──────────────────────────────────────────────────────

    private fun loadTodayPanchang(location: LocationData) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val tz = TimeZone.currentSystemDefault()
                val today = Clock.System.now().toLocalDateTime(tz).date
                val noon = kotlinx.datetime.LocalDateTime(
                    today.year, today.month.number, today.day, 12, 0, 0
                ).toInstant(tz)
                val panchang = panchangService.calculate(
                    date = today,
                    lat = location.latitude,
                    lon = location.longitude,
                    locationName = location.displayName,
                    observationInstant = noon
                )
                _state.update { it.copy(todayPanchang = PanchangUiState.Success(panchang)) }
                triggerSideEffects(panchang, today, location)
            } catch (e: Exception) {
                _state.update { it.copy(todayPanchang = PanchangUiState.Error(e.message ?: "Calculation failed")) }
            }
        }
    }

    // ── Live Panchang (1-minute tick) ─────────────────────────────────────────

    private fun startLiveTick() {
        liveTickJob?.cancel()
        liveTickJob = viewModelScope.launch {
            while (true) {
                val now = Clock.System.now()
                _state.update { it.copy(liveNow = now) }
                val locState = _state.value.location
                if (locState is LocationUiState.Success) {
                    launch(Dispatchers.Default) {
                        try {
                            val tz = TimeZone.currentSystemDefault()
                            val today = now.toLocalDateTime(tz).date
                            val panchang = panchangService.calculate(
                                date = today,
                                lat = locState.location.latitude,
                                lon = locState.location.longitude,
                                locationName = locState.location.displayName,
                                observationInstant = now
                            )
                            _state.update { it.copy(livePanchang = PanchangUiState.Success(panchang)) }
                        } catch (_: Exception) {}
                    }
                }
                delay(60_000L)
            }
        }
    }

    // ── Selected date ─────────────────────────────────────────────────────────

    fun selectDate(date: LocalDate) {
        _state.update { it.copy(selectedDate = date, selectedPanchang = PanchangUiState.Loading) }
        loadSelectedPanchang()
    }

    private fun loadSelectedPanchang(locationOverride: LocationData? = null) {
        val location = locationOverride ?: currentLocation() ?: LocationData.DEFAULT
        val date = _state.value.selectedDate
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val panchang = panchangService.calculate(date, location.latitude, location.longitude, location.displayName)
                _state.update { it.copy(selectedPanchang = PanchangUiState.Success(panchang)) }
            } catch (e: Exception) {
                _state.update { it.copy(selectedPanchang = PanchangUiState.Error(e.message ?: "Calculation failed")) }
            }
        }
    }

    // ── Arbitrary date ────────────────────────────────────────────────────────

    /**
     * Compute Panchang for any date without changing the selected date.
     * Used by the calendar to pre-load month data.
     */
    suspend fun getPanchangForDate(date: LocalDate, locationOverride: LocationData? = null): PanchangModel? {
        val loc = locationOverride ?: currentLocation() ?: LocationData.DEFAULT
        return withContext(Dispatchers.Default) {
            runCatching {
                panchangService.calculate(date, loc.latitude, loc.longitude, loc.displayName)
            }.getOrNull()
        }
    }

    private fun currentLocation(): LocationData? {
        val locState = _state.value.location
        return if (locState is LocationUiState.Success) locState.location else null
    }

    private fun isSameLocation(a: LocationData, b: LocationData): Boolean {
        val latDiff = abs(a.latitude - b.latitude)
        val lonDiff = abs(a.longitude - b.longitude)
        return latDiff < 0.0001 && lonDiff < 0.0001 && a.displayName == b.displayName
    }

    // ── Side effects ──────────────────────────────────────────────────────────

    private fun triggerSideEffects(panchang: PanchangModel, today: LocalDate, location: LocationData) {
        viewModelScope.launch {
            if (lastWidgetSyncDate != today) {
                lastWidgetSyncDate = today
                runCatching { widgetService.updateWidget(panchang) }
            }
            if (lastNotifSyncDate != today) {
                lastNotifSyncDate = today
                val settings = preferences.notificationSettings.first()
                val locale = preferences.locale.first()
                launch(Dispatchers.Default) {
                    runCatching {
                        notificationScheduler.reschedule(
                            lat = location.latitude,
                            lon = location.longitude,
                            locationName = location.displayName,
                            settings = settings,
                            locale = locale
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        liveTickJob?.cancel()
    }
}
