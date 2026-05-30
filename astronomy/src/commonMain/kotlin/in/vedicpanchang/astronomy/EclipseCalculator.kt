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
     * Accepts tithiIndex within ±1 of Purnima (14) to tolerate the limbTime observation
     * moment being offset from the actual opposition — e.g. when Purnima ends just before
     * sunrise, the limb falls in Krishna Pratipada (15) instead.
     */
    fun isLunarEclipse(jd: Double, tithiIndex: Int): Boolean {
        if (tithiIndex !in (PURNIMA_INDEX - 1)..(PURNIMA_INDEX + 1)) return false
        return abs(AstronomyService.moonLatitude(jd)) < LUNAR_ECLIPSE_LAT_LIMIT
    }

    /**
     * Returns true if a solar eclipse is possible at the given Julian Day.
     * Accepts tithiIndex within ±1 of Amavasya (29), including wrap-around to 0
     * (Shukla Pratipada) when Amavasya ends before the limbTime observation moment.
     */
    fun isSolarEclipse(jd: Double, tithiIndex: Int): Boolean {
        val nearAmavasya = tithiIndex in (AMAVASYA_INDEX - 1)..AMAVASYA_INDEX || tithiIndex == 0
        if (!nearAmavasya) return false
        return abs(AstronomyService.moonLatitude(jd)) < SOLAR_ECLIPSE_LAT_LIMIT
    }
}
