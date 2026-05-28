package `in`.vedicpanchang.app.data.model

import kotlin.time.Clock
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.time.Instant

/** Birth input for a horoscope chart. */
data class BirthDetails(
    val birthInstant: Instant,   // UTC birth moment
    val latitude: Double,
    val longitude: Double,
    val locationName: String
)

/** One period in the Vimshottari Dasha system. */
data class DashaPeriod(
    val planet: String,
    val start: Instant,
    val end: Instant,
    val totalYears: Int,
    val antardashas: List<DashaPeriod> = emptyList()
) {
    val isCurrent: Boolean
        get() = Clock.System.now().let { now -> now >= start && now < end }
}

/** Single planet's position in the birth chart. */
data class PlanetData(
    val name: String,
    val symbol: String,
    val siderealLongitude: Double,  // 0–360° sidereal
    val signIndex: Int,              // 0–11 (Aries–Pisces)
    val signName: String,
    val degreeInSign: Double,        // 0–30°
    val houseNumber: Int,            // 1–12 (whole-sign)
    val nakshatraIndex: Int,         // 0–26
    val nakshatraName: String,
    val nakshatraLord: String,
    val isRetrograde: Boolean
) {
    val degreeStr: String
        get() {
            val deg = floor(degreeInSign).toInt()
            val min = ((degreeInSign - deg) * 60).roundToInt()
            return "$deg° $min′"
        }
}

/** Full Vedic birth chart (Kundali / Janma Kundali). */
data class HoroscopeModel(
    val birthDetails: BirthDetails,

    // Ascendant (Lagna)
    val lagnaLongitude: Double,
    val lagnaSignIndex: Int,
    val lagnaSignName: String,
    val lagnaDegreeInSign: Double,
    val lagnaNakshatraIndex: Int,
    val lagnaNakshatraName: String,

    // Midheaven (MC)
    val mcLongitude: Double,
    val mcSignIndex: Int,
    val mcSignName: String,

    // Nine Navagraha planets
    val planets: List<PlanetData>,

    // Whole-sign house signs (index 0 = house 1 sign index, ..., index 11 = house 12)
    val houseSigns: List<Int>,

    // Navamsha (D-9)
    val lagnaNavamshaSignIndex: Int,
    val navamshaData: List<PlanetData>,
    val navamshaHouseSigns: List<Int>,

    // Vimshottari Dasha periods
    val dashas: List<DashaPeriod>
) {
    fun planetsInHouse(house: Int): List<PlanetData> =
        planets.filter { it.houseNumber == house }
}
