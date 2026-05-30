package `in`.vedicpanchang.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.vedicpanchang.app.data.datasource.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val country: String
) {
    val displayName: String get() = if (country.isNotEmpty()) "$cityName, $country" else cityName

    companion object {
        val DEFAULT = LocationData(28.6139, 77.2090, "New Delhi", "India")
    }
}

/**
 * Wraps FusedLocationProviderClient and Geocoder.
 * Equivalent of location_service.dart. All suspend functions are safe to call
 * from Dispatchers.IO.
 */
@Singleton
class LocationService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: AppPreferences
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    val cachedLocation: Flow<LocationData> = preferences.cachedLocation
        .map { it?.let { c -> LocationData(c.latitude, c.longitude, c.city, c.country) }
            ?: LocationData.DEFAULT }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData {
        return getCurrentLocationOrNull() ?: LocationData.DEFAULT
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationOrNull(): LocationData? {
        return try {
            // Phase 1: balanced accuracy (WiFi/cell) with a 10s timeout — fast first fix
            val cts = CancellationTokenSource()
            val position = withTimeoutOrNull(10_000L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                        .addOnSuccessListener { loc: Location? -> cont.resume(loc) }
                        .addOnFailureListener { cont.resume(null) }
                    cont.invokeOnCancellation { cts.cancel() }
                }
            }
            // Phase 2: fall back to last cached device location if Phase 1 timed out or failed
            ?: suspendCancellableCoroutine<Location?> { cont ->
                fusedClient.lastLocation
                    .addOnSuccessListener { loc: Location? -> cont.resume(loc) }
                    .addOnFailureListener { cont.resume(null) }
            }
            ?: return null

            val result = reverseGeocode(position.latitude, position.longitude)
            preferences.saveCachedLocation(
                AppPreferences.CachedLocation(result.latitude, result.longitude, result.cityName, result.country)
            )
            result
        } catch (_: Exception) {
            null
        }
    }

    suspend fun searchLocation(query: String): LocationData? {
        if (query.isBlank()) return null
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocationName(query.trim(), 1) { cont.resume(it) }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query.trim(), 1) ?: emptyList()
            }
            if (addresses.isEmpty()) return null
            val addr = addresses.first()
            LocationData(
                latitude = addr.latitude,
                longitude = addr.longitude,
                cityName = addr.locality ?: addr.subAdminArea ?: query.trim(),
                country = addr.countryName ?: ""
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun reverseGeocode(lat: Double, lon: Double): LocationData {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1) { cont.resume(it) }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lon, 1) ?: emptyList()
            }
            if (addresses.isEmpty()) return LocationData(lat, lon, "Unknown", "Unknown")
            val addr = addresses.first()
            LocationData(
                latitude = lat,
                longitude = lon,
                cityName = addr.locality ?: addr.subAdminArea ?: "Unknown",
                country = addr.countryName ?: "Unknown"
            )
        } catch (_: Exception) {
            LocationData(lat, lon, "Unknown", "Unknown")
        }
    }
}
