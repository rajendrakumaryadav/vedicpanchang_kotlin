package `in`.vedicpanchang.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.vedicpanchang.app.data.model.BirthDetails
import `in`.vedicpanchang.app.data.model.HoroscopeModel
import `in`.vedicpanchang.app.service.HoroscopeService
import `in`.vedicpanchang.app.service.LocationData
import `in`.vedicpanchang.app.service.LocationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface HoroscopeUiState {
    data object Idle : HoroscopeUiState
    data object Loading : HoroscopeUiState
    data class Success(val chart: HoroscopeModel) : HoroscopeUiState
    data class Error(val message: String) : HoroscopeUiState
}

sealed interface LocationSearchState {
    data object Idle : LocationSearchState
    data object Searching : LocationSearchState
    data class Found(val location: LocationData) : LocationSearchState
    data object NotFound : LocationSearchState
}

data class HoroscopeScreenState(
    val birthDetails: BirthDetails? = null,
    val chart: HoroscopeUiState = HoroscopeUiState.Idle,
    val isSouthIndianStyle: Boolean = false,
    val locationSearch: LocationSearchState = LocationSearchState.Idle
)

/**
 * Manages Kundali/horoscope chart state.
 * Equivalent of horoscope_provider.dart + chartStyleProvider + horoscopeInputProvider.
 */
@HiltViewModel
class HoroscopeViewModel @Inject constructor(
    private val horoscopeService: HoroscopeService,
    private val locationService: LocationService
) : ViewModel() {

    private val _state = MutableStateFlow(HoroscopeScreenState())
    val state: StateFlow<HoroscopeScreenState> = _state.asStateFlow()

    // ── Chart calculation ─────────────────────────────────────────────────────

    fun calculateChart(details: BirthDetails) {
        _state.update { it.copy(birthDetails = details, chart = HoroscopeUiState.Loading) }
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val chart = horoscopeService.calculateChart(details)
                _state.update { it.copy(chart = HoroscopeUiState.Success(chart)) }
            } catch (e: Exception) {
                _state.update { it.copy(chart = HoroscopeUiState.Error(e.message ?: "Chart calculation failed")) }
            }
        }
    }

    fun clearChart() {
        _state.update { it.copy(birthDetails = null, chart = HoroscopeUiState.Idle) }
    }

    // ── Chart style ───────────────────────────────────────────────────────────

    fun setChartStyle(isSouthIndian: Boolean) {
        _state.update { it.copy(isSouthIndianStyle = isSouthIndian) }
    }

    fun toggleChartStyle() {
        _state.update { it.copy(isSouthIndianStyle = !it.isSouthIndianStyle) }
    }

    // ── Birth place search ────────────────────────────────────────────────────

    fun searchBirthPlace(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(locationSearch = LocationSearchState.Idle) }
            return
        }
        _state.update { it.copy(locationSearch = LocationSearchState.Searching) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                locationService.searchLocation(query)
            }
            _state.update {
                it.copy(
                    locationSearch = if (result != null) LocationSearchState.Found(result)
                                    else LocationSearchState.NotFound
                )
            }
        }
    }

    fun useCurrentLocation() {
        _state.update { it.copy(locationSearch = LocationSearchState.Searching) }
        viewModelScope.launch(Dispatchers.IO) {
            val location = locationService.getCurrentLocation()
            _state.update { it.copy(locationSearch = LocationSearchState.Found(location)) }
        }
    }

    fun clearLocationSearch() {
        _state.update { it.copy(locationSearch = LocationSearchState.Idle) }
    }
}
