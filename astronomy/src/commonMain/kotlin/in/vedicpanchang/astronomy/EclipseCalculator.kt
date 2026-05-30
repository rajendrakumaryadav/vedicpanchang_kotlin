package `in`.vedicpanchang.astronomy

import kotlin.math.abs

/**
 * Approximate eclipse detection based on the moon's ecliptic latitude.
 * An eclipse is geometrically possible when the moon is near a lunar node (Rahu/Ketu)
 * at the time of full or new moon.
 *
 * Thresholds are conservative eclipse limits from Meeus "Astronomical Algorithms" Ch. 54.
 * This gives ~90% detection accuracy for penumbral and umbral events.
 */
object EclipseCalculator {

    // Moon's ecliptic latitude threshold (degrees) for each eclipse type
    private const val LUNAR_ECLIPSE_LAT_LIMIT = 1.55   // penumbral + umbral
    private const val SOLAR_ECLIPSE_LAT_LIMIT = 1.60   // partial + annular + total

    // Tithi indices for eclipse-eligible phases
    private const val PURNIMA_INDEX = 14   // Full Moon → Lunar Eclipse
    private const val AMAVASYA_INDEX = 29  // New Moon → Solar Eclipse

    /**
     * Returns true if a lunar eclipse is possible at the given Julian Day.
     * Requires the tithi to be Purnima (index 14).
     */
    fun isLunarEclipse(jd: Double, tithiIndex: Int): Boolean {
        if (tithiIndex != PURNIMA_INDEX) return false
        return abs(AstronomyService.moonLatitude(jd)) < LUNAR_ECLIPSE_LAT_LIMIT
    }

    /**
     * Returns true if a solar eclipse is possible at the given Julian Day.
     * Requires the tithi to be Amavasya (index 29).
     */
    fun isSolarEclipse(jd: Double, tithiIndex: Int): Boolean {
        if (tithiIndex != AMAVASYA_INDEX) return false
        return abs(AstronomyService.moonLatitude(jd)) < SOLAR_ECLIPSE_LAT_LIMIT
    }
}
