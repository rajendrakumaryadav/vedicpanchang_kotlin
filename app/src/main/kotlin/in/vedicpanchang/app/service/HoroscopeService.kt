package `in`.vedicpanchang.app.service

import `in`.vedicpanchang.app.data.model.*
import `in`.vedicpanchang.astronomy.AstronomyService
import `in`.vedicpanchang.astronomy.PanchangConstants
import `in`.vedicpanchang.astronomy.PlanetaryPositions
import kotlin.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

private val RASHI_NAMES = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

private val NAKSHATRA_LORDS = listOf(
    "Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter",
    "Saturn", "Mercury", "Ketu", "Venus", "Sun", "Moon", "Mars",
    "Rahu", "Jupiter", "Saturn", "Mercury", "Ketu", "Venus", "Sun",
    "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"
)

private val PLANET_SYMBOLS = mapOf(
    "Sun" to "☀️", "Moon" to "🌙", "Mars" to "♂️", "Mercury" to "☿",
    "Jupiter" to "♃", "Venus" to "♀️", "Saturn" to "♄", "Rahu" to "☊", "Ketu" to "☋"
)

private val DASHA_SEQUENCE = listOf("Ketu","Venus","Sun","Moon","Mars","Rahu","Jupiter","Saturn","Mercury")
private val DASHA_YEARS   = listOf(7, 20, 6, 10, 7, 18, 16, 19, 17)

private const val DEG2RAD = PI / 180.0
private const val RAD2DEG = 180.0 / PI
private const val NAKSHATRA_SPAN = 360.0 / 27.0

/**
 * Orchestrates the complete Vedic birth chart calculation.
 * Equivalent of horoscope_service.dart. CPU-bound — call from Dispatchers.Default.
 */
@Singleton
class HoroscopeService @Inject constructor() {

    fun calculateChart(details: BirthDetails): HoroscopeModel {
        val jd = AstronomyService.julianDayFromInstant(details.birthInstant)
        val ayanamsha = AstronomyService.lahiriAyanamsha(jd)

        // Sun & Moon sidereal
        val sunTropical = AstronomyService.sunLongitude(jd)
        val moonTropical = AstronomyService.moonLongitude(jd)
        val sunSidereal = normAngle(sunTropical - ayanamsha)
        val moonSidereal = normAngle(moonTropical - ayanamsha)

        // Other planets tropical → sidereal
        val otherTropical = PlanetaryPositions.tropicalLongitudes(jd, sunTropical)
        val siderealLons = mutableMapOf("Sun" to sunSidereal, "Moon" to moonSidereal)
        otherTropical.forEach { (name, lon) -> siderealLons[name] = normAngle(lon - ayanamsha) }

        // Ascendant & MC
        val lagnaLonTropical = calculateAscendant(jd, details.latitude, details.longitude)
        val lagnaLon = normAngle(lagnaLonTropical - ayanamsha)
        val lagnaSign = signIndex(lagnaLon)

        val mcTropical = calculateMC(jd, details.longitude)
        val mcLon = normAngle(mcTropical - ayanamsha)
        val mcSign = signIndex(mcLon)

        // Whole-sign houses
        val houseSigns = List(12) { i -> (lagnaSign + i) % 12 }

        // Build planet list
        val planetOrder = listOf("Sun","Moon","Mars","Mercury","Jupiter","Venus","Saturn","Rahu","Ketu")
        val planets = planetOrder.map { name ->
            val lon = siderealLons[name]!!
            val sign = signIndex(lon)
            val nakIdx = floor(lon / NAKSHATRA_SPAN).toInt().coerceIn(0, 26)
            PlanetData(
                name = name,
                symbol = PLANET_SYMBOLS[name] ?: "",
                siderealLongitude = lon,
                signIndex = sign,
                signName = RASHI_NAMES[sign],
                degreeInSign = lon - sign * 30.0,
                houseNumber = wholeSignHouse(sign, lagnaSign),
                nakshatraIndex = nakIdx,
                nakshatraName = PanchangConstants.NAKSHATRA_NAMES[nakIdx],
                nakshatraLord = NAKSHATRA_LORDS[nakIdx],
                isRetrograde = PlanetaryPositions.isRetrograde(name, jd)
            )
        }

        val lagnaNakIdx = floor(lagnaLon / NAKSHATRA_SPAN).toInt().coerceIn(0, 26)

        // Navamsha (D-9)
        val lagnaNavSign = navamshaSignIndex(lagnaLon)
        val navamshaHouseSigns = List(12) { i -> (lagnaNavSign + i) % 12 }
        val navamshaData = planets.map { p ->
            val navSign = navamshaSignIndex(p.siderealLongitude)
            p.copy(signIndex = navSign, signName = RASHI_NAMES[navSign], houseNumber = wholeSignHouse(navSign, lagnaNavSign))
        }

        // Vimshottari Dasha
        val dashas = calculateDashas(details.birthInstant, siderealLons["Moon"]!!)

        return HoroscopeModel(
            birthDetails = details,
            lagnaLongitude = lagnaLon, lagnaSignIndex = lagnaSign, lagnaSignName = RASHI_NAMES[lagnaSign],
            lagnaDegreeInSign = lagnaLon - lagnaSign * 30.0,
            lagnaNakshatraIndex = lagnaNakIdx, lagnaNakshatraName = PanchangConstants.NAKSHATRA_NAMES[lagnaNakIdx],
            mcLongitude = mcLon, mcSignIndex = mcSign, mcSignName = RASHI_NAMES[mcSign],
            planets = planets, houseSigns = houseSigns,
            lagnaNavamshaSignIndex = lagnaNavSign, navamshaData = navamshaData, navamshaHouseSigns = navamshaHouseSigns,
            dashas = dashas
        )
    }

    // ── Ascendant ─────────────────────────────────────────────────────────────

    private fun calculateAscendant(jd: Double, lat: Double, lon: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val epsilon = obliquityCorrection(t)
        val lstDeg = localSiderealTimeDeg(jd, lon)
        val lstRad = lstDeg * DEG2RAD
        val epsRad = epsilon * DEG2RAD
        val latRad = lat * DEG2RAD
        var asc = atan2(cos(lstRad), -(sin(lstRad) * cos(epsRad) + tan(latRad) * sin(epsRad))) * RAD2DEG
        asc = normAngle(asc)
        if (normAngle(asc - lstDeg) > 180.0) asc = normAngle(asc + 180.0)
        return asc
    }

    private fun calculateMC(jd: Double, lon: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val epsilon = obliquityCorrection(t)
        val lstRad = localSiderealTimeDeg(jd, lon) * DEG2RAD
        return normAngle(atan2(sin(lstRad), cos(lstRad) * cos(epsilon * DEG2RAD)) * RAD2DEG)
    }

    private fun localSiderealTimeDeg(jd: Double, lon: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val gmst = 280.46061837 + 360.98564736629 * (jd - 2451545.0) + 0.000387933 * t * t - (t * t * t) / 38710000.0
        return normAngle(gmst + lon)
    }

    private fun obliquityCorrection(t: Double): Double {
        val sec = 21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))
        val mean = 23.0 + (26.0 + sec / 60.0) / 60.0
        return mean + 0.00256 * cos((125.04 - 1934.136 * t) * DEG2RAD)
    }

    // ── Navamsha ──────────────────────────────────────────────────────────────

    private fun navamshaSignIndex(longitude: Double): Int {
        val signIdx = floor(longitude / 30.0).toInt().coerceIn(0, 11)
        val degInSign = longitude - signIdx * 30.0
        val navPos = floor(degInSign * 9 / 30.0).toInt().coerceIn(0, 8)
        val startSigns = listOf(0, 9, 6, 3)  // Fire, Earth, Air, Water
        return (startSigns[signIdx % 4] + navPos) % 12
    }

    // ── Vimshottari Dasha ─────────────────────────────────────────────────────

    private fun calculateDashas(birthInstant: Instant, moonLon: Double): List<DashaPeriod> {
        val nakIdx = floor(moonLon / NAKSHATRA_SPAN).toInt().coerceIn(0, 26)
        val posWithinNak = (moonLon % NAKSHATRA_SPAN) / NAKSHATRA_SPAN
        val startSeqIdx = nakIdx % 9
        val elapsedDays = (posWithinNak * DASHA_YEARS[startSeqIdx] * 365.25).toLong()
        var currentStart = Instant.fromEpochMilliseconds(birthInstant.toEpochMilliseconds() - elapsedDays * 86400_000L)

        return List(9) { i ->
            val seqIdx = (startSeqIdx + i) % 9
            val planet = DASHA_SEQUENCE[seqIdx]
            val years = DASHA_YEARS[seqIdx]
            val end = Instant.fromEpochMilliseconds(currentStart.toEpochMilliseconds() + (years * 365.25 * 86400_000L).toLong())

            val antardashas = List(9) { j ->
                val adSeqIdx = (seqIdx + j) % 9
                val adPlanet = DASHA_SEQUENCE[adSeqIdx]
                val adYears = DASHA_YEARS[adSeqIdx]
                val adDays = (years * adYears * 365.25 / 120.0).toLong()
                DashaPeriod(adPlanet, currentStart, Instant.fromEpochMilliseconds(currentStart.toEpochMilliseconds() + adDays * 86400_000L), adYears)
            }.let { ads ->
                // Fix: rebuild antardashas with sequential start times
                val result = mutableListOf<DashaPeriod>()
                var adCurrent = currentStart
                for (j in 0 until 9) {
                    val adSeqIdx = (seqIdx + j) % 9
                    val adPlanet = DASHA_SEQUENCE[adSeqIdx]
                    val adYears = DASHA_YEARS[adSeqIdx]
                    val adDays = (years * adYears * 365.25 / 120.0).toLong()
                    val adEnd = Instant.fromEpochMilliseconds(adCurrent.toEpochMilliseconds() + adDays * 86400_000L)
                    result.add(DashaPeriod(adPlanet, adCurrent, adEnd, adYears))
                    adCurrent = adEnd
                }
                result
            }

            val period = DashaPeriod(planet, currentStart, end, years, antardashas)
            currentStart = end
            period
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private fun normAngle(angle: Double): Double {
        var r = angle % 360.0
        if (r < 0.0) r += 360.0
        return r
    }

    private fun signIndex(lon: Double): Int = floor(lon / 30.0).toInt().coerceIn(0, 11)

    private fun wholeSignHouse(planetSign: Int, lagnaSign: Int): Int =
        ((planetSign - lagnaSign + 12) % 12) + 1
}
