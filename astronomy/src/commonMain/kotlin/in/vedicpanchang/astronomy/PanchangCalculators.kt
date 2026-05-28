package `in`.vedicpanchang.astronomy

import kotlinx.datetime.Instant
import kotlin.math.floor

// ─── Tithi ───────────────────────────────────────────────────────────────────

object TithiCalculator {

    fun calculateTithiIndex(sunLon: Double, moonLon: Double): Int {
        val diff = (moonLon - sunLon + 360.0) % 360.0
        return floor(diff / 12.0).toInt().coerceIn(0, 29)
    }

    fun tithiName(index: Int): String = PanchangConstants.TITHI_NAMES[index]

    fun paksha(tithiIndex: Int): String =
        if (tithiIndex < 15) "Shukla" else "Krishna"

    fun tithiStartTime(referenceTime: Instant, tithiIndex: Int): Instant {
        val startJd = AstronomyService.julianDayFromInstant(referenceTime)
        return findPreviousTransition(startJd, tithiIndex) { jd ->
            val sun = AstronomyService.sunLongitudeSidereal(jd)
            val moon = AstronomyService.moonLongitudeSidereal(jd)
            calculateTithiIndex(sun, moon)
        }
    }

    fun tithiEndTime(referenceTime: Instant, tithiIndex: Int): Instant {
        val startJd = AstronomyService.julianDayFromInstant(referenceTime)
        return findNextTransition(startJd, tithiIndex) { jd ->
            val sun = AstronomyService.sunLongitudeSidereal(jd)
            val moon = AstronomyService.moonLongitudeSidereal(jd)
            calculateTithiIndex(sun, moon)
        }
    }
}

// ─── Nakshatra ────────────────────────────────────────────────────────────────

object NakshatraCalculator {

    private const val NAKSHATRA_SPAN = 360.0 / 27.0  // ~13.333°

    fun calculateNakshatraIndex(moonLon: Double): Int =
        floor(moonLon / NAKSHATRA_SPAN).toInt().coerceIn(0, 26)

    fun nakshatraName(index: Int): String = PanchangConstants.NAKSHATRA_NAMES[index]

    fun nakshatraElapsed(moonLon: Double): Double = moonLon % NAKSHATRA_SPAN

    fun nakshatraStartTime(referenceTime: Instant, nakshatraIndex: Int): Instant {
        val startJd = AstronomyService.julianDayFromInstant(referenceTime)
        return findPreviousTransition(startJd, nakshatraIndex) { jd ->
            calculateNakshatraIndex(AstronomyService.moonLongitudeSidereal(jd))
        }
    }

    fun nakshatraEndTime(referenceTime: Instant, nakshatraIndex: Int): Instant {
        val startJd = AstronomyService.julianDayFromInstant(referenceTime)
        return findNextTransition(startJd, nakshatraIndex) { jd ->
            calculateNakshatraIndex(AstronomyService.moonLongitudeSidereal(jd))
        }
    }
}

// ─── Yoga ─────────────────────────────────────────────────────────────────────

object YogaCalculator {

    private const val YOGA_SPAN = 360.0 / 27.0

    fun calculateYogaIndex(sunLon: Double, moonLon: Double): Int {
        val combined = (sunLon + moonLon) % 360.0
        return floor(combined / YOGA_SPAN).toInt().coerceIn(0, 26)
    }

    fun yogaName(index: Int): String = PanchangConstants.YOGA_NAMES[index]

    fun isAuspicious(name: String): Boolean = PanchangConstants.YOGA_AUSPICIOUS[name] ?: true

    fun yogaStartTime(referenceTime: Instant, yogaIndex: Int): Instant {
        val startJd = AstronomyService.julianDayFromInstant(referenceTime)
        return findPreviousTransition(startJd, yogaIndex) { jd ->
            val sun = AstronomyService.sunLongitudeSidereal(jd)
            val moon = AstronomyService.moonLongitudeSidereal(jd)
            calculateYogaIndex(sun, moon)
        }
    }

    fun yogaEndTime(referenceTime: Instant, yogaIndex: Int): Instant {
        val startJd = AstronomyService.julianDayFromInstant(referenceTime)
        return findNextTransition(startJd, yogaIndex) { jd ->
            val sun = AstronomyService.sunLongitudeSidereal(jd)
            val moon = AstronomyService.moonLongitudeSidereal(jd)
            calculateYogaIndex(sun, moon)
        }
    }
}

// ─── Karana ───────────────────────────────────────────────────────────────────

object KaranaCalculator {

    fun calculateKaranaIndex(sunLon: Double, moonLon: Double): Int {
        val diff = (moonLon - sunLon + 360.0) % 360.0
        val halfTithi = floor(diff / 6.0).toInt()
        return when (halfTithi) {
            0 -> 10   // Kimstughna (fixed)
            57 -> 7   // Shakuni (fixed)
            58 -> 8   // Chatushpada (fixed)
            59 -> 9   // Naga (fixed)
            else -> (halfTithi - 1) % 7  // 7 movable
        }
    }

    fun karanaName(index: Int): String = PanchangConstants.KARANA_NAMES[index]

    fun karanaStartTime(referenceTime: Instant, karanaIndex: Int): Instant {
        val startJd = AstronomyService.julianDayFromInstant(referenceTime)
        return findPreviousTransition(startJd, karanaIndex) { jd ->
            val sun = AstronomyService.sunLongitudeSidereal(jd)
            val moon = AstronomyService.moonLongitudeSidereal(jd)
            calculateKaranaIndex(sun, moon)
        }
    }

    fun karanaEndTime(referenceTime: Instant, karanaIndex: Int): Instant {
        val startJd = AstronomyService.julianDayFromInstant(referenceTime)
        return findNextTransition(startJd, karanaIndex) { jd ->
            val sun = AstronomyService.sunLongitudeSidereal(jd)
            val moon = AstronomyService.moonLongitudeSidereal(jd)
            calculateKaranaIndex(sun, moon)
        }
    }
}

// ─── Transition finders (binary search, ±72-hour scan) ───────────────────────

private fun findPreviousTransition(
    startJd: Double,
    startIndex: Int,
    indexAtJd: (Double) -> Int
): Instant {
    var right = startJd
    for (hour in 1..72) {
        val leftBound = startJd - hour / 24.0
        if (indexAtJd(leftBound) != startIndex) {
            var left = leftBound
            var rightBound = right
            repeat(28) {
                val mid = (left + rightBound) / 2.0
                if (indexAtJd(mid) == startIndex) rightBound = mid else left = mid
            }
            return AstronomyService.jdToInstant(rightBound)
        }
        right = leftBound
    }
    return AstronomyService.jdToInstant(startJd - 1.0)
}

private fun findNextTransition(
    startJd: Double,
    startIndex: Int,
    indexAtJd: (Double) -> Int
): Instant {
    var low = startJd
    var current = startIndex
    for (hour in 1..72) {
        val high = startJd + hour / 24.0
        val idx = indexAtJd(high)
        if (idx != current) {
            var left = low
            var right = high
            repeat(28) {
                val mid = (left + right) / 2.0
                if (indexAtJd(mid) == current) left = mid else right = mid
            }
            return AstronomyService.jdToInstant(right)
        }
        low = high
        current = idx
    }
    return AstronomyService.jdToInstant(startJd + 1.0)
}
