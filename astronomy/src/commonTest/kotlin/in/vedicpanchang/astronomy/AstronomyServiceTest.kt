package `in`.vedicpanchang.astronomy

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Reference values verified against Drik Panchang and traditional tables
 * for New Delhi (28.6139°N, 77.2090°E), UTC+5:30.
 */
class AstronomyServiceTest {

    // Reference date: 2025-01-15 at noon IST = 06:30 UTC
    private val REFERENCE_INSTANT = LocalDateTime(2025, 1, 15, 6, 30, 0)
        .toInstant(TimeZone.UTC)

    private val DELHI_LAT = 28.6139
    private val DELHI_LON = 77.2090

    @Test
    fun julianDay_knownValue() {
        // J2000.0 = 2000-01-01 12:00 UTC → JD 2451545.0
        val jd = AstronomyService.julianDay(2000, 1, 1, 12.0)
        assertEquals(2451545.0, jd, 0.0001)
    }

    @Test
    fun julianDay_epoch1970() {
        // Unix epoch 1970-01-01 00:00 UTC → JD 2440587.5
        val jd = AstronomyService.julianDay(1970, 1, 1, 0.0)
        assertEquals(2440587.5, jd, 0.0001)
    }

    @Test
    fun sunLongitude_inRange() {
        val jd = AstronomyService.julianDayFromInstant(REFERENCE_INSTANT)
        val lon = AstronomyService.sunLongitude(jd)
        assertTrue(lon >= 0.0 && lon < 360.0, "Sun longitude $lon out of [0,360)")
        // Mid-January: tropical sun is in Capricorn ~295° range
        assertTrue(lon in 290.0..305.0, "Sun tropical longitude $lon not in expected Capricorn range")
    }

    @Test
    fun lahiriAyanamsha_approxValue() {
        // For J2000.0, Lahiri ayanamsha ≈ 23.85° (from precession baseline)
        val jd = AstronomyService.julianDay(2000, 1, 1, 12.0)
        val ayan = AstronomyService.lahiriAyanamsha(jd)
        assertTrue(ayan in 23.5..24.5, "Lahiri ayanamsha $ayan out of expected range ~23.85°")
    }

    @Test
    fun tithiIndex_knownDate() {
        // 2025-01-13 is Purnima (Full Moon) — tithi index should be 14
        val purnima = LocalDateTime(2025, 1, 13, 18, 0, 0).toInstant(TimeZone.UTC)
        val jd = AstronomyService.julianDayFromInstant(purnima)
        val sunLon = AstronomyService.sunLongitudeSidereal(jd)
        val moonLon = AstronomyService.moonLongitudeSidereal(jd)
        val index = TithiCalculator.calculateTithiIndex(sunLon, moonLon)
        // Around full moon, moon–sun diff ≈ 180° → tithi 14 (Purnima) or transitioning
        assertTrue(index in 13..15, "Tithi index $index not near Purnima on 2025-01-13")
    }

    @Test
    fun nakshatraIndex_inRange() {
        val jd = AstronomyService.julianDayFromInstant(REFERENCE_INSTANT)
        val moonLon = AstronomyService.moonLongitudeSidereal(jd)
        val index = NakshatraCalculator.calculateNakshatraIndex(moonLon)
        assertTrue(index in 0..26, "Nakshatra index $index out of [0,26]")
    }

    @Test
    fun yogaIndex_inRange() {
        val jd = AstronomyService.julianDayFromInstant(REFERENCE_INSTANT)
        val sunLon = AstronomyService.sunLongitudeSidereal(jd)
        val moonLon = AstronomyService.moonLongitudeSidereal(jd)
        val index = YogaCalculator.calculateYogaIndex(sunLon, moonLon)
        assertTrue(index in 0..26, "Yoga index $index out of [0,26]")
    }

    @Test
    fun karanaIndex_inRange() {
        val jd = AstronomyService.julianDayFromInstant(REFERENCE_INSTANT)
        val sunLon = AstronomyService.sunLongitudeSidereal(jd)
        val moonLon = AstronomyService.moonLongitudeSidereal(jd)
        val index = KaranaCalculator.calculateKaranaIndex(sunLon, moonLon)
        assertTrue(index in 0..10, "Karana index $index out of [0,10]")
    }

    @Test
    fun sunriseSunset_delhiWinter() {
        // Delhi winter sunrise ≈ 07:12 IST (01:42 UTC), sunset ≈ 17:42 IST (12:12 UTC)
        val date = LocalDate(2025, 1, 15)
        val result = AstronomyService.sunriseSunset(date, DELHI_LAT, DELHI_LON)

        val sunriseUtcHour = result.sunrise.toEpochMilliseconds() / 3600_000.0 % 24
        val sunsetUtcHour = result.sunset.toEpochMilliseconds() / 3600_000.0 % 24

        // UTC sunrise should be between 01:00 and 03:00
        assertTrue(sunriseUtcHour in 1.0..3.0, "Delhi sunrise UTC hour $sunriseUtcHour not in expected range")
        // UTC sunset should be between 11:00 and 13:00
        assertTrue(sunsetUtcHour in 11.0..13.0, "Delhi sunset UTC hour $sunsetUtcHour not in expected range")
        assertTrue(result.sunset > result.sunrise, "Sunset must be after sunrise")
    }

    @Test
    fun moonriseMoonset_returnsValidTimes() {
        val date = LocalDate(2025, 1, 15)
        val result = AstronomyService.moonriseMoonset(date, DELHI_LAT, DELHI_LON)
        // Just verify non-null valid instants
        assertTrue(result.moonrise.toEpochMilliseconds() > 0)
        assertTrue(result.moonset.toEpochMilliseconds() > 0)
    }

    @Test
    fun choghadiya_produces14Slots() {
        val date = LocalDate(2025, 1, 15)
        val ss = AstronomyService.sunriseSunset(date, DELHI_LAT, DELHI_LON)
        val slots = ChoghadiyaCalculator.calculate(ss.sunrise, ss.sunset, 3) // Wednesday
        assertEquals(14, slots.size, "Expected 7 day + 7 night choghadiya slots")
        assertEquals(7, slots.count { it.isDay })
        assertEquals(7, slots.count { !it.isDay })
    }

    @Test
    fun hora_produces24Slots() {
        val date = LocalDate(2025, 1, 15)
        val ss = AstronomyService.sunriseSunset(date, DELHI_LAT, DELHI_LON)
        val slots = HoraCalculator.calculate(ss.sunrise, ss.sunset, 3)
        assertEquals(24, slots.size, "Expected 24 hora slots")
        assertEquals(12, slots.count { it.isDay })
        assertEquals(12, slots.count { !it.isDay })
    }

    @Test
    fun planetaryPositions_allKeysPresent() {
        val jd = AstronomyService.julianDayFromInstant(REFERENCE_INSTANT)
        val sunTropical = AstronomyService.sunLongitude(jd)
        val positions = PlanetaryPositions.tropicalLongitudes(jd, sunTropical)
        val expected = setOf("Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Rahu", "Ketu")
        assertEquals(expected, positions.keys)
        positions.values.forEach { lon ->
            assertTrue(lon >= 0.0 && lon < 360.0, "Planetary longitude $lon out of [0,360)")
        }
    }

    @Test
    fun normalizeAngle_handlesNegative() {
        assertEquals(270.0, AstronomyService.normalizeAngle(-90.0), 0.001)
        assertEquals(0.0, AstronomyService.normalizeAngle(360.0), 0.001)
        assertEquals(180.0, AstronomyService.normalizeAngle(540.0), 0.001)
    }
}
