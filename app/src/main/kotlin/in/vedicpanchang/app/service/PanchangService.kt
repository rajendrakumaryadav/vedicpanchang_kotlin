package `in`.vedicpanchang.app.service

import `in`.vedicpanchang.app.data.datasource.FestivalData
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.astronomy.*
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlin.math.floor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

/**
 * High-level service that orchestrates all Panchang calculations
 * and returns a complete [PanchangModel] for a given date and location.
 *
 * Equivalent of panchang_service.dart. Uses a 512-entry LRU cache.
 * All work is CPU-bound — callers should use Dispatchers.Default.
 */
@Singleton
class PanchangService @Inject constructor() {

    private val cache = LinkedHashMap<String, PanchangModel>()

    @Synchronized
    fun calculate(
        date: LocalDate,
        lat: Double,
        lon: Double,
        locationName: String,
        observationInstant: Instant? = null
    ): PanchangModel {
        val key = cacheKey(date, lat, lon, locationName, observationInstant)
        cache[key]?.let { return it }

        // 1. Sunrise / Sunset
        val ss = AstronomyService.sunriseSunset(date, lat, lon)
        val sunrise = ss.sunrise
        val sunset = ss.sunset

        // 2. Determine limbTime — observation moment for the Panchang limbs.
        //    Noon-tithi rule: if the sunrise tithi ends before local noon, use noon.
        val limbInstant: Instant = if (observationInstant != null) {
            observationInstant
        } else {
            val sunriseJd = AstronomyService.julianDayFromInstant(sunrise)
            val sunriseSunLon = AstronomyService.sunLongitudeSidereal(sunriseJd)
            val sunriseMoonLon = AstronomyService.moonLongitudeSidereal(sunriseJd)
            val sunriseTithiIdx = TithiCalculator.calculateTithiIndex(sunriseSunLon, sunriseMoonLon)
            val sunriseTithiEnd = TithiCalculator.tithiEndTime(sunrise, sunriseTithiIdx)
            val localNoon = LocalDateTime(date.year, date.month.number, date.day, 12, 0, 0)
                .toInstant(TimeZone.currentSystemDefault())
            if (sunriseTithiEnd < localNoon) localNoon else sunrise
        }

        val jd = AstronomyService.julianDayFromInstant(limbInstant)
        val sunLon = AstronomyService.sunLongitudeSidereal(jd)
        val moonLon = AstronomyService.moonLongitudeSidereal(jd)

        // 3. Tithi
        val tithiIdx = TithiCalculator.calculateTithiIndex(sunLon, moonLon)
        val tithiN = TithiCalculator.tithiName(tithiIdx)
        val pakshaStr = TithiCalculator.paksha(tithiIdx)
        val tithiStart = TithiCalculator.tithiStartTime(limbInstant, tithiIdx)
        val tithiEnd = TithiCalculator.tithiEndTime(limbInstant, tithiIdx)

        // 4. Nakshatra
        val nakshatraIdx = NakshatraCalculator.calculateNakshatraIndex(moonLon)
        val nakshatraN = NakshatraCalculator.nakshatraName(nakshatraIdx)
        val nakshatraStart = NakshatraCalculator.nakshatraStartTime(limbInstant, nakshatraIdx)
        val nakshatraEnd = NakshatraCalculator.nakshatraEndTime(limbInstant, nakshatraIdx)

        // 5. Yoga
        val yogaIdx = YogaCalculator.calculateYogaIndex(sunLon, moonLon)
        val yogaN = YogaCalculator.yogaName(yogaIdx)
        val yogaAuspicious = YogaCalculator.isAuspicious(yogaN)
        val yogaStart = YogaCalculator.yogaStartTime(limbInstant, yogaIdx)
        val yogaEnd = YogaCalculator.yogaEndTime(limbInstant, yogaIdx)

        // 6. Karana
        val karanaIdx = KaranaCalculator.calculateKaranaIndex(sunLon, moonLon)
        val karanaN = KaranaCalculator.karanaName(karanaIdx)
        val karanaStart = KaranaCalculator.karanaStartTime(limbInstant, karanaIdx)
        val karanaChange = KaranaCalculator.karanaEndTime(limbInstant, karanaIdx)
        val karanaNxt = KaranaCalculator.karanaName((karanaIdx + 1) % 11)

        // 7. Vaar
        date.dayOfWeek.ordinal % 7  // 0=Sun..6=Sat (kotlinx-datetime: Mon=0)
        val vaar = PanchangConstants.VAAR_NAMES[
            // kotlinx-datetime DayOfWeek: MONDAY=0..SUNDAY=6; convert to Sun=0..Sat=6
            (date.dayOfWeek.ordinal + 1) % 7
        ]

        // 8. Moonrise / Moonset
        val mm = AstronomyService.moonriseMoonset(date, lat, lon)

        // 9. Muhurtas
        val brahma = MuhurtaCalculator.brahmaMuhurta(sunrise)
        val abhijit = MuhurtaCalculator.abhijitMuhurta(sunrise, sunset)
        val daytimeMuhurtas = MuhurtaCalculator.daytimeMuhurtas(sunrise, sunset)
        val auspiciousMuhurtas = MuhurtaCalculator.auspiciousMuhurtas(sunrise, sunset)

        // 10. Inauspicious periods (weekday in kotlinx-datetime: Monday=1..Sunday=7)
        val weekday = date.dayOfWeek.isoDayNumber  // 1=Mon..7=Sun  (matches Dart convention)
        val rahu = MuhurtaCalculator.rahuKaal(sunrise, sunset, weekday)
        val yama = MuhurtaCalculator.yamaganda(sunrise, sunset, weekday)
        val gulika = MuhurtaCalculator.gulikaKaal(sunrise, sunset, weekday)

        // 11. Festivals
        val festivals = FestivalData.getFestivals(
            tithiIndex = tithiIdx,
            month = date.month.number,
            sunLon = sunLon,
            moonLon = moonLon
        )

        // 12. Lunar month index (0=Chaitra … 11=Phalguna)
        val normalizedSunLon = ((sunLon % 360.0) + 360.0) % 360.0
        val lunarMonthIdx = (floor(normalizedSunLon / 30.0).toInt() + 1) % 12

        val model = PanchangModel(
            date = date, latitude = lat, longitude = lon, locationName = locationName,
            tithiIndex = tithiIdx, tithiName = tithiN, paksha = pakshaStr,
            tithiStartTime = tithiStart, tithiEndTime = tithiEnd,
            nakshatraIndex = nakshatraIdx, nakshatraName = nakshatraN,
            nakshatraStartTime = nakshatraStart, nakshatraEndTime = nakshatraEnd,
            yogaIndex = yogaIdx, yogaName = yogaN, isYogaAuspicious = yogaAuspicious,
            yogaStartTime = yogaStart, yogaEndTime = yogaEnd,
            karanaIndex = karanaIdx, karanaName = karanaN, karanaNext = karanaNxt,
            karanaStartTime = karanaStart, karanaChangeTime = karanaChange,
            vaarName = vaar,
            sunrise = sunrise, sunset = sunset,
            moonrise = mm?.moonrise ?: Instant.DISTANT_FUTURE,
            moonset = mm?.moonset ?: Instant.DISTANT_FUTURE,
            brahmaMuhurta = brahma, abhijitMuhurta = abhijit,
            auspiciousMuhurtas = auspiciousMuhurtas, daytimeMuhurtas = daytimeMuhurtas,
            rahuKaal = rahu, yamaganda = yama, gulikaKaal = gulika,
            festivals = festivals,
            sunLongitude = sunLon, moonLongitude = moonLon,
            lunarMonthIndex = lunarMonthIdx
        )

        cache[key] = model
        if (cache.size > MAX_CACHE) cache.remove(cache.keys.first())
        return model
    }

    private fun cacheKey(
        date: LocalDate, lat: Double, lon: Double,
        locationName: String, observationInstant: Instant?
    ): String = "${date}|${"%.8f".format(lat)}|${"%.8f".format(lon)}|$locationName|$observationInstant"

    companion object {
        private const val MAX_CACHE = 512
    }
}
