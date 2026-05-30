package `in`.vedicpanchang.astronomy

import kotlin.math.abs
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Location-aware eclipse detection.
 *
 * Two improvements over a simple tithi-index check:
 *
 * 1. Precise timing — the exact opposition (Purnima) or conjunction (Amavasya) is found via
 *    binary search, so the eclipse fires only on the correct local calendar day, never ±1 day.
 *
 * 2. Observer visibility — a lunar eclipse requires the moon to be above the horizon;
 *    a solar eclipse requires the sun to be above the horizon at the observer's location.
 *
 * Latitude thresholds from Meeus "Astronomical Algorithms" Ch. 54.
 */
object EclipseCalculator {

    private const val LUNAR_ECLIPSE_LAT_LIMIT = 1.55   // penumbral + umbral
    private const val SOLAR_ECLIPSE_LAT_LIMIT = 1.60   // partial + annular + total

    private const val PURNIMA_INDEX  = 14   // Full Moon  → Lunar Eclipse
    private const val AMAVASYA_INDEX = 29   // New Moon   → Solar Eclipse

    /**
     * Returns true when a lunar eclipse is possible on [date] as seen from [lat]/[lon].
     *
     * Requires:
     *  - tithi within ±1 of Purnima (tolerates limbTime offset from exact opposition)
     *  - exact opposition falls on the observer's local [date]
     *  - moon's ecliptic latitude at opposition is below the penumbral threshold
     *  - moon is above the horizon at [lat]/[lon] at the time of opposition
     */
    fun isLunarEclipse(
        jd: Double, tithiIndex: Int,
        lat: Double, lon: Double,
        date: LocalDate
    ): Boolean {
        if (tithiIndex !in (PURNIMA_INDEX - 1)..(PURNIMA_INDEX + 1)) return false
        val purnimaJd = findExactOpposition(jd) ?: return false
        if (abs(AstronomyService.moonLatitude(purnimaJd)) >= LUNAR_ECLIPSE_LAT_LIMIT) return false
        if (!isSameLocalDate(purnimaJd, date)) return false
        return AstronomyService.moonAltitude(purnimaJd, lat, lon) > 0.0
    }

    /**
     * Returns true when a solar eclipse is possible on [date] as seen from [lat]/[lon].
     *
     * Requires:
     *  - tithi within ±1 of Amavasya (tolerates limbTime offset from exact conjunction)
     *  - exact conjunction falls on the observer's local [date]
     *  - moon's ecliptic latitude at conjunction is below the solar threshold
     *  - sun is above the horizon at [lat]/[lon] at the time of conjunction
     */
    fun isSolarEclipse(
        jd: Double, tithiIndex: Int,
        lat: Double, lon: Double,
        date: LocalDate
    ): Boolean {
        val nearAmavasya = tithiIndex == AMAVASYA_INDEX ||
                           tithiIndex == AMAVASYA_INDEX - 1 ||
                           tithiIndex == 0   // Amavasya ended before limbTime → now Shukla Pratipada
        if (!nearAmavasya) return false
        val conjunctionJd = findExactConjunction(jd) ?: return false
        if (abs(AstronomyService.moonLatitude(conjunctionJd)) >= SOLAR_ECLIPSE_LAT_LIMIT) return false
        if (!isSameLocalDate(conjunctionJd, date)) return false
        return AstronomyService.sunAltitude(conjunctionJd, lat, lon) > 0.0
    }

    // ─── Binary search helpers ────────────────────────────────────────────────

    /**
     * Finds the Julian Day of the exact full moon (elongation = 180°) within ±2 days of [jd].
     * Returns null if no full moon is found in that window.
     */
    private fun findExactOpposition(jd: Double): Double? {
        fun elongMinus180(j: Double): Double {
            val m = AstronomyService.moonLongitudeSidereal(j)
            val s = AstronomyService.sunLongitudeSidereal(j)
            return ((m - s + 360.0) % 360.0) - 180.0
        }
        var a = jd - 2.0; var b = jd + 2.0
        var ea = elongMinus180(a)
        val eb = elongMinus180(b)
        if (ea * eb > 0) return null   // 180° not in this window
        repeat(50) {
            val mid = (a + b) / 2.0
            val em = elongMinus180(mid)
            if (ea * em <= 0) b = mid else { a = mid; ea = em }
        }
        return (a + b) / 2.0
    }

    /**
     * Finds the Julian Day of the exact new moon (elongation ≈ 0°) within ±2 days of [jd].
     * Elongation is folded to −180..180 so the zero crossing is found cleanly near Amavasya.
     * Returns null if no new moon is found in that window.
     */
    private fun findExactConjunction(jd: Double): Double? {
        fun foldedElong(j: Double): Double {
            val m = AstronomyService.moonLongitudeSidereal(j)
            val s = AstronomyService.sunLongitudeSidereal(j)
            val e = (m - s + 360.0) % 360.0
            return if (e > 180.0) e - 360.0 else e   // fold 350° → -10°, etc.
        }
        var a = jd - 2.0; var b = jd + 2.0
        var ea = foldedElong(a)
        val eb = foldedElong(b)
        if (ea * eb > 0) return null   // 0° not in this window
        repeat(50) {
            val mid = (a + b) / 2.0
            val em = foldedElong(mid)
            if (ea * em <= 0) b = mid else { a = mid; ea = em }
        }
        return (a + b) / 2.0
    }

    // ─── Date comparison ──────────────────────────────────────────────────────

    /**
     * Converts a Julian Day (UT) to the observer's local calendar date using the
     * device timezone and checks whether it matches [date].
     */
    private fun isSameLocalDate(jd: Double, date: LocalDate): Boolean {
        val instant: Instant = AstronomyService.jdToInstant(jd)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return localDate == date
    }
}
