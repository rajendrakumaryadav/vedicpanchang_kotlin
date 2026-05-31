package `in`.vedicpanchang.app.data.model

import `in`.vedicpanchang.astronomy.MuhurtaPeriod
import `in`.vedicpanchang.astronomy.TimeRange
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** Complete Vedic calendar data for one day and location. */
data class PanchangModel(
    val date: LocalDate,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,

    // Tithi
    val tithiIndex: Int,          // 0–29
    val tithiName: String,
    val paksha: String,            // "Shukla" | "Krishna"
    val tithiStartTime: Instant,
    val tithiEndTime: Instant,

    // Nakshatra
    val nakshatraIndex: Int,       // 0–26
    val nakshatraName: String,
    val nakshatraStartTime: Instant,
    val nakshatraEndTime: Instant,

    // Yoga
    val yogaIndex: Int,            // 0–26
    val yogaName: String,
    val isYogaAuspicious: Boolean,
    val yogaStartTime: Instant,
    val yogaEndTime: Instant,

    // Karana
    val karanaIndex: Int,
    val karanaName: String,
    val karanaNext: String,
    val karanaStartTime: Instant,
    val karanaChangeTime: Instant,

    // Vaar (weekday)
    val vaarName: String,

    // Sun & Moon
    val sunrise: Instant,
    val sunset: Instant,
    val moonrise: Instant,
    val moonset: Instant,

    // Muhurtas — auspicious
    val brahmaMuhurta: TimeRange,
    val abhijitMuhurta: TimeRange,
    val auspiciousMuhurtas: List<MuhurtaPeriod>,
    val daytimeMuhurtas: List<MuhurtaPeriod>,
    val nighttimeMuhurtas: List<MuhurtaPeriod>,

    // Inauspicious periods
    val rahuKaal: TimeRange,
    val yamaganda: TimeRange,
    val gulikaKaal: TimeRange,
    val durmuhurtas: List<TimeRange>,
    val varjyams: List<TimeRange>,

    // Festivals / events
    val festivals: List<String>,

    // Raw astronomical values for display
    val sunLongitude: Double,
    val moonLongitude: Double,

    // Vedic lunar month index (0=Chaitra … 11=Phalguna)
    val lunarMonthIndex: Int,

    // Adhikmash (intercalary lunar month): true when no solar sankranti in this lunar month
    val isAdhikmash: Boolean,
    val adhikmashName: String?,

    // Eclipse possibility for this day
    val lunarEclipse: Boolean,
    val solarEclipse: Boolean
) {
    val tithiDisplay: String get() = "$paksha $tithiName"
    val tithiNumber: Int get() = tithiIndex + 1
    val hasFestivals: Boolean get() = festivals.isNotEmpty()
    val primaryFestival: String? get() = festivals.firstOrNull()
    val hasEclipse: Boolean get() = lunarEclipse || solarEclipse
}
