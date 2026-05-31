package `in`.vedicpanchang.astronomy

import kotlin.time.Instant

/**
 * Choghadiya calculator based on authentic Vedic rules.
 * Day (sunrise–sunset) is divided into 7 equal slots; night also 7 slots.
 */
object ChoghadiyaCalculator {

    private val CYCLE_ORDER = listOf("Udveg", "Char", "Labh", "Amrit", "Kaal", "Shubh", "Rog")

    // Sunday=0..Saturday=6
    private val DAY_START_INDEX = listOf(0, 3, 6, 2, 5, 1, 4)
    private val NIGHT_START_INDEX = listOf(5, 1, 4, 0, 3, 6, 2)

    val TYPES = mapOf(
        "Amrit" to ChoghadiyaType.VERY_AUSPICIOUS,
        "Shubh" to ChoghadiyaType.AUSPICIOUS,
        "Labh" to ChoghadiyaType.GOOD,
        "Char" to ChoghadiyaType.NEUTRAL,
        "Rog" to ChoghadiyaType.INAUSPICIOUS,
        "Kaal" to ChoghadiyaType.INAUSPICIOUS,
        "Udveg" to ChoghadiyaType.INAUSPICIOUS
    )

    /** Returns 8 day slots + 8 night slots. weekday: 1=Mon..7=Sun. */
    fun calculate(sunrise: Instant, sunset: Instant, weekday: Int): List<ChoghadiyaSlot> {
        val dayIndex = weekday % 7  // 0=Sun..6=Sat

        val dayDuration = sunset - sunrise
        val slotDuration = dayDuration / 8

        val nextSunrise = Instant.fromEpochMilliseconds(sunrise.toEpochMilliseconds() + 86400_000L)
        val nightDuration = nextSunrise - sunset
        val nightSlotDuration = nightDuration / 8

        val slots = mutableListOf<ChoghadiyaSlot>()

        val dayStart = DAY_START_INDEX[dayIndex]
        for (i in 0 until 8) {
            val name = CYCLE_ORDER[(dayStart + i) % 7]
            val start = sunrise + slotDuration * i
            val end = if (i == 7) sunset else sunrise + slotDuration * (i + 1)
            slots.add(ChoghadiyaSlot(name, TYPES[name]!!, start, end, true))
        }

        val nightStart = NIGHT_START_INDEX[dayIndex]
        for (i in 0 until 8) {
            val name = CYCLE_ORDER[(nightStart + i) % 7]
            val start = sunset + nightSlotDuration * i
            val end = if (i == 7) nextSunrise else sunset + nightSlotDuration * (i + 1)
            slots.add(ChoghadiyaSlot(name, TYPES[name]!!, start, end, false))
        }

        return slots
    }

    fun currentSlot(slots: List<ChoghadiyaSlot>, now: Instant): ChoghadiyaSlot? =
        slots.firstOrNull { now >= it.start && now < it.end }

    // ── Localization ──────────────────────────────────────────────────────────

    private val LOCALIZED_NAMES = mapOf(
        "hi" to mapOf("Amrit" to "अमृत", "Shubh" to "शुभ", "Labh" to "लाभ",
            "Char" to "चर", "Rog" to "रोग", "Kaal" to "काल", "Udveg" to "उद्वेग"),
        "sa" to mapOf("Amrit" to "अमृतम्", "Shubh" to "शुभम्", "Labh" to "लाभः",
            "Char" to "चरम्", "Rog" to "रोगः", "Kaal" to "कालः", "Udveg" to "उद्वेगः")
    )

    fun localizeName(name: String, locale: String): String =
        if (locale == "en") name else LOCALIZED_NAMES[locale]?.get(name) ?: name
}

enum class ChoghadiyaType { VERY_AUSPICIOUS, AUSPICIOUS, GOOD, NEUTRAL, INAUSPICIOUS }

data class ChoghadiyaSlot(
    val name: String,
    val type: ChoghadiyaType,
    val start: Instant,
    val end: Instant,
    val isDay: Boolean
) {
    val isAuspicious: Boolean
        get() = type == ChoghadiyaType.VERY_AUSPICIOUS ||
                type == ChoghadiyaType.AUSPICIOUS ||
                type == ChoghadiyaType.GOOD
}
