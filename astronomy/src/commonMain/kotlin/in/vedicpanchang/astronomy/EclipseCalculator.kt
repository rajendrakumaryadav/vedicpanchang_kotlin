package `in`.vedicpanchang.astronomy

import kotlin.math.abs
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Global eclipse detection — reports every eclipse that occurs on the given date,
 * regardless of whether it is visible from the observer's location.
 *
 * Two improvements over a simple tithi-index check:
 *
 * 1. Precise timing — the exact opposition (Purnima) or conjunction (Amavasya) is found via
 *    binary search (50 iterations, ~nanosecond precision), so the eclipse fires only on the
 *    correct local calendar day, never ±1 day.
 *
 * 2. Geometric filter — the moon's ecliptic latitude at the moment of opposition/conjunction
 *    must be within the umbral/penumbral shadow cone (Meeus Ch. 54 thresholds). This prevents
 *    false positives on every Purnima/Amavasya.
 *
 * Visibility (altitude above horizon) is intentionally NOT checked: Vedic panchang treats an
 * eclipse as a global astronomical event that affects the entire tithi, regardless of local
 * horizon visibility.
 */
object EclipseCalculator {

    // Meeus "Astronomical Algorithms" Ch. 54 — penumbral + partial + total/annular
    private const val LUNAR_ECLIPSE_LAT_LIMIT = 1.57   // degrees; penumbral shadow limit
    private const val SOLAR_ECLIPSE_LAT_LIMIT = 1.57   // degrees; partial eclipse limit

    private const val PURNIMA_INDEX  = 14   // Full Moon  → Lunar Eclipse
    private const val AMAVASYA_INDEX = 29   // New Moon   → Solar Eclipse

    /**
     * Returns true when a lunar eclipse occurs on [date].
     *
     * Requires:
     *  - tithi within ±1 of Purnima (tolerates limbTime offset from exact opposition)
     *  - exact opposition falls on the local [date] (device timezone)
     *  - moon's ecliptic latitude at opposition is within the penumbral shadow cone
     */
    fun isLunarEclipse(jd: Double, tithiIndex: Int, date: LocalDate): Boolean {
        if (tithiIndex !in (PURNIMA_INDEX - 1)..(PURNIMA_INDEX + 1)) return false
        val purnimaJd = findExactOpposition(jd) ?: return false
        if (abs(AstronomyService.moonLatitude(purnimaJd)) >= LUNAR_ECLIPSE_LAT_LIMIT) return false
        return isSameLocalDate(purnimaJd, date)
    }

    /**
     * Returns true when a solar eclipse occurs on [date].
     *
     * Requires:
     *  - tithi within ±1 of Amavasya (tolerates limbTime offset from exact conjunction)
     *  - exact conjunction falls on the local [date] (device timezone)
     *  - moon's ecliptic latitude at conjunction is within the partial-eclipse shadow cone
     */
    fun isSolarEclipse(jd: Double, tithiIndex: Int, date: LocalDate): Boolean {
        val nearAmavasya = tithiIndex == AMAVASYA_INDEX ||
                           tithiIndex == AMAVASYA_INDEX - 1 ||
                           tithiIndex == 0   // Amavasya ended before limbTime → now Shukla Pratipada
        if (!nearAmavasya) return false
        val conjunctionJd = findExactConjunction(jd) ?: return false
        if (abs(AstronomyService.moonLatitude(conjunctionJd)) >= SOLAR_ECLIPSE_LAT_LIMIT) return false
        return isSameLocalDate(conjunctionJd, date)
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
