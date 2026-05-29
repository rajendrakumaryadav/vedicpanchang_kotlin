package `in`.vedicpanchang.astronomy

import kotlin.time.Instant
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.microseconds

/**
 * Calculates auspicious (Muhurta) and inauspicious periods
 * (Rahu Kaal, Yamaganda, Gulika Kaal) based on sunrise/sunset.
 */
object MuhurtaCalculator {

    private val DAYTIME_MUHURTA_IDS = listOf(
        "rudra_muhurta", "ahi_muhurta", "mitra_muhurta", "pitru_muhurta",
        "vasu_muhurta", "varaha_muhurta", "vishwadeva_muhurta", "vidhi_muhurta",
        "satamukhi_muhurta", "puruhuta_muhurta", "vahni_muhurta", "naktanchara_muhurta",
        "varuna_muhurta", "aryama_muhurta", "bhaga_muhurta"
    )

    // ─── Inauspicious periods ─────────────────────────────────────────────────

    /** Rahu Kaal. weekday: 1=Mon..7=Sun (matches Dart/kotlinx-datetime weekday). */
    fun rahuKaal(sunrise: Instant, sunset: Instant, weekday: Int): TimeRange {
        val dayIndex = weekday % 7  // 0=Sun..6=Sat
        return dayBlockRange(sunrise, sunset, PanchangConstants.RAHU_KAAL_BLOCKS[dayIndex])
    }

    fun yamaganda(sunrise: Instant, sunset: Instant, weekday: Int): TimeRange {
        val dayIndex = weekday % 7
        return dayBlockRange(sunrise, sunset, PanchangConstants.YAMAGANDA_BLOCKS[dayIndex])
    }

    fun gulikaKaal(sunrise: Instant, sunset: Instant, weekday: Int): TimeRange {
        val dayIndex = weekday % 7
        return dayBlockRange(sunrise, sunset, PanchangConstants.GULIKA_BLOCKS[dayIndex])
    }

    // ─── Auspicious muhurtas ──────────────────────────────────────────────────

    /** Brahma Muhurta — ~96 minutes before sunrise. */
    fun brahmaMuhurta(sunrise: Instant): TimeRange {
        val end = sunrise - 24.minutes
        val start = end - 48.minutes
        return safeRange(start, end)
    }

    /** Abhijit Muhurta — midday muhurta (8th of 15 daytime muhurtas). */
    fun abhijitMuhurta(sunrise: Instant, sunset: Instant): TimeRange {
        val (s, e) = orderedRange(sunrise, sunset)
        val dayMicros = (e - s).inWholeMicroseconds
        val halfDayMicros = (dayMicros / 2.0).roundToLong()
        val noon = s + halfDayMicros.microseconds
        val muhurtaMicros = (dayMicros / 15.0).roundToLong()
        val halfMuhurtaMicros = (muhurtaMicros / 2.0).roundToLong()
        return safeRange(noon - halfMuhurtaMicros.microseconds, noon + halfMuhurtaMicros.microseconds)
    }

    /** Vijaya Muhurta — 11th daytime muhurta. */
    fun vijayaMuhurta(sunrise: Instant, sunset: Instant): TimeRange =
        dayMuhurtaRange(sunrise, sunset, 11)

    /** Godhuli Muhurta — ±24 min around sunset. */
    fun godhuliMuhurta(sunset: Instant): TimeRange =
        safeRange(sunset - 24.minutes, sunset + 24.minutes)

    /** All 15 daytime muhurtas from sunrise to sunset. */
    fun daytimeMuhurtas(sunrise: Instant, sunset: Instant): List<MuhurtaPeriod> =
        List(DAYTIME_MUHURTA_IDS.size) { index ->
            MuhurtaPeriod(DAYTIME_MUHURTA_IDS[index], dayMuhurtaRange(sunrise, sunset, index + 1))
        }

    /** Commonly-used auspicious muhurtas. */
    fun auspiciousMuhurtas(sunrise: Instant, sunset: Instant): List<MuhurtaPeriod> = listOf(
        MuhurtaPeriod("abhijit_muhurta", abhijitMuhurta(sunrise, sunset)),
        MuhurtaPeriod("vijaya_muhurta", vijayaMuhurta(sunrise, sunset)),
        MuhurtaPeriod("godhuli_muhurta", godhuliMuhurta(sunset))
    )

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun orderedRange(sunrise: Instant, sunset: Instant): Pair<Instant, Instant> {
        val s = if (!sunset.isAfter(sunrise)) sunrise else sunrise
        val e = if (!sunset.isAfter(sunrise))
            Instant.fromEpochMilliseconds(sunset.toEpochMilliseconds() + 86400_000L)
        else sunset
        return s to e
    }

    /** Exact 1/8-day block using microsecond precision. */
    private fun dayBlockRange(sunrise: Instant, sunset: Instant, block: Int): TimeRange {
        val (s, e) = orderedRange(sunrise, sunset)
        val totalMicros = (e - s).inWholeMicroseconds
        val startMicros = (totalMicros * (block - 1) / 8.0).roundToLong()
        val endMicros = (totalMicros * block / 8.0).roundToLong()
        return safeRange(s + startMicros.microseconds, s + endMicros.microseconds)
    }

    private fun dayMuhurtaRange(sunrise: Instant, sunset: Instant, muhurtaNumber: Int): TimeRange {
        val (s, e) = orderedRange(sunrise, sunset)
        val totalMicros = (e - s).inWholeMicroseconds
        val startMicros = (totalMicros * (muhurtaNumber - 1) / 15.0).roundToLong()
        val endMicros = (totalMicros * muhurtaNumber / 15.0).roundToLong()
        return safeRange(s + startMicros.microseconds, s + endMicros.microseconds)
    }

    private fun safeRange(a: Instant, b: Instant): TimeRange =
        if (a > b) TimeRange(b, a) else TimeRange(a, b)
}

private fun Instant.isAfter(other: Instant): Boolean = this > other
