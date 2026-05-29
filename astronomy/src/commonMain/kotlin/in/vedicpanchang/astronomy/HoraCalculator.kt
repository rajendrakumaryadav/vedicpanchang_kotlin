package `in`.vedicpanchang.astronomy

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/**
 * Hora (Planetary Hour) calculator based on the Chaldean order.
 * Day has 12 horas (sunrise–sunset), night has 12 (sunset–next sunrise).
 */
object HoraCalculator {

    private val CHALDEAN_ORDER = listOf("Saturn", "Jupiter", "Mars", "Sun", "Venus", "Mercury", "Moon")

    // Sunday=0 → Sun(3), Monday=1 → Moon(6), Tuesday=2 → Mars(2), etc.
    private val WEEKDAY_LORD_INDEX = listOf(3, 6, 2, 5, 1, 4, 0)

    val PLANET_SYMBOLS = mapOf(
        "Sun" to "☀️", "Moon" to "🌙", "Mars" to "♂️",
        "Mercury" to "☿", "Jupiter" to "♃", "Venus" to "♀️", "Saturn" to "♄"
    )

    /** Calculate all 24 horas using the real next-day sunrise. */
    fun calculateForDate(
        date: LocalDate,
        sunrise: Instant,
        sunset: Instant,
        weekday: Int,
        lat: Double,
        lon: Double
    ): List<HoraSlot> {
        val nextDay = LocalDate(date.year, date.month.number, date.day + 1)
        val nextSunrise = AstronomyService.sunriseSunset(nextDay, lat, lon).sunrise
        return calculate(sunrise, sunset, weekday, nextSunrise)
    }

    fun calculate(
        sunrise: Instant,
        sunset: Instant,
        weekday: Int,
        nextSunrise: Instant? = null
    ): List<HoraSlot> {
        val dayIndex = weekday % 7  // 0=Sun..6=Sat
        val startIdx = WEEKDAY_LORD_INDEX[dayIndex]

        val dayDuration = sunset - sunrise
        val dayHoraDuration = dayDuration / 12

        val resolvedNextSunrise = nextSunrise
            ?: Instant.fromEpochMilliseconds(sunrise.toEpochMilliseconds() + 86400_000L)
        val nightDuration = resolvedNextSunrise - sunset
        val nightHoraDuration = nightDuration / 12

        val slots = mutableListOf<HoraSlot>()

        for (i in 0 until 12) {
            val planetIdx = (startIdx + i) % 7
            val planet = CHALDEAN_ORDER[planetIdx]
            val start = sunrise + dayHoraDuration * i
            val end = if (i == 11) sunset else sunrise + dayHoraDuration * (i + 1)
            slots.add(HoraSlot(planet, PLANET_SYMBOLS[planet]!!, start, end, true, i + 1))
        }

        for (i in 0 until 12) {
            val planetIdx = (startIdx + 12 + i) % 7
            val planet = CHALDEAN_ORDER[planetIdx]
            val start = sunset + nightHoraDuration * i
            val end = if (i == 11) resolvedNextSunrise else sunset + nightHoraDuration * (i + 1)
            slots.add(HoraSlot(planet, PLANET_SYMBOLS[planet]!!, start, end, false, i + 13))
        }

        return slots
    }

    fun currentHora(slots: List<HoraSlot>, now: Instant): HoraSlot? =
        slots.firstOrNull { now >= it.start && now < it.end }

    // ── Localization ──────────────────────────────────────────────────────────

    private val LOCALIZED_NAMES = mapOf(
        "hi" to mapOf("Sun" to "सूर्य", "Moon" to "चंद्र", "Mars" to "मंगल",
            "Mercury" to "बुध", "Jupiter" to "गुरु", "Venus" to "शुक्र", "Saturn" to "शनि"),
        "sa" to mapOf("Sun" to "सूर्यः", "Moon" to "चन्द्रः", "Mars" to "मङ्गलः",
            "Mercury" to "बुधः", "Jupiter" to "गुरुः", "Venus" to "शुक्रः", "Saturn" to "शनिः")
    )

    fun localizeName(planet: String, locale: String): String =
        if (locale == "en") planet else LOCALIZED_NAMES[locale]?.get(planet) ?: planet
}

data class HoraSlot(
    val planet: String,
    val symbol: String,
    val start: Instant,
    val end: Instant,
    val isDay: Boolean,
    val horaNumber: Int
)
