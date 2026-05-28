package `in`.vedicpanchang.app.l10n

import `in`.vedicpanchang.app.data.datasource.FestivalData
import `in`.vedicpanchang.app.data.model.PanchangModel

/**
 * Central utility to localize all Panchang data values.
 * Every screen should use this instead of raw PanchangModel fields when displaying user-visible text.
 * Equivalent of panchang_localizer.dart.
 */
class PanchangLocalizer(val locale: String) {

    private val strings = AppStrings.of(locale)

    private val DEVA_DIGITS = listOf('०','१','२','३','४','५','६','७','८','९')

    // ── Tithi ─────────────────────────────────────────────────────────────────

    fun tithiDisplay(p: PanchangModel): String {
        if (locale == "en") return p.tithiDisplay
        val tithis = AppStrings.TITHI_NAMES[locale] ?: return p.tithiDisplay
        if (p.tithiIndex >= tithis.size) return p.tithiDisplay
        val paksha = if (p.tithiIndex < 15) strings["shukla"]!! else strings["krishna"]!!
        return "$paksha ${tithis[p.tithiIndex]}"
    }

    fun tithiName(p: PanchangModel): String {
        if (locale == "en") return p.tithiName
        val tithis = AppStrings.TITHI_NAMES[locale] ?: return p.tithiName
        return if (p.tithiIndex < tithis.size) tithis[p.tithiIndex] else p.tithiName
    }

    fun paksha(p: PanchangModel): String {
        if (locale == "en") return p.paksha
        return if (p.tithiIndex < 15) strings["shukla"]!! else strings["krishna"]!!
    }

    // ── Nakshatra ─────────────────────────────────────────────────────────────

    fun nakshatraName(p: PanchangModel): String {
        if (locale == "en") return p.nakshatraName
        val names = AppStrings.NAKSHATRA_NAMES[locale] ?: return p.nakshatraName
        return if (p.nakshatraIndex < names.size) names[p.nakshatraIndex] else p.nakshatraName
    }

    // ── Yoga ──────────────────────────────────────────────────────────────────

    fun yogaName(p: PanchangModel): String {
        if (locale == "en") return p.yogaName
        val names = AppStrings.YOGA_NAMES[locale] ?: return p.yogaName
        return if (p.yogaIndex < names.size) names[p.yogaIndex] else p.yogaName
    }

    fun yogaAuspiciousLabel(p: PanchangModel): String =
        if (p.isYogaAuspicious) "✓ ${strings["auspicious"]}" else "✗ ${strings["inauspicious"]}"

    fun yogaWithAuspicious(p: PanchangModel): String {
        val name = yogaName(p)
        val label = if (p.isYogaAuspicious) "(${strings["auspicious"]})" else "(${strings["inauspicious"]})"
        return "$name $label"
    }

    // ── Karana ────────────────────────────────────────────────────────────────

    fun karanaName(p: PanchangModel): String {
        if (locale == "en") return p.karanaName
        val names = AppStrings.KARANA_NAMES[locale] ?: return p.karanaName
        return if (p.karanaIndex < names.size) names[p.karanaIndex] else p.karanaName
    }

    fun karanaNext(p: PanchangModel): String {
        if (locale == "en") return p.karanaNext
        val nextIdx = (p.karanaIndex + 1) % 11
        val names = AppStrings.KARANA_NAMES[locale] ?: return p.karanaNext
        return if (nextIdx < names.size) names[nextIdx] else p.karanaNext
    }

    // ── Vaar ──────────────────────────────────────────────────────────────────

    fun vaarName(p: PanchangModel): String {
        val vaars = AppStrings.VAAR_NAMES[locale] ?: return p.vaarName
        // Mon=0..Sun=6 in ordinal → Sun=0..Sat=6 index
        val idx = (p.date.dayOfWeek.ordinal + 1) % 7
        return vaars[idx]
    }

    // ── Planets ──────────────────────────────────────────────────────────────

    fun planetName(name: String): String {
        if (locale == "en") return name
        return AppStrings.PLANET_NAMES[locale]?.get(name) ?: name
    }

    // ── Choghadiya ───────────────────────────────────────────────────────────

    fun choghadiyaName(name: String): String =
        `in`.vedicpanchang.astronomy.ChoghadiyaCalculator.localizeName(name, locale)

    // ── Festivals ─────────────────────────────────────────────────────────────

    fun festivalName(englishName: String): String {
        if (locale == "en") return englishName
        return FestivalData.getLocalizedName(englishName, locale)
    }

    fun festivals(p: PanchangModel): List<String> = p.festivals.map { festivalName(it) }

    // ── Vedic Calendar ────────────────────────────────────────────────────────

    fun vedicMonthName(p: PanchangModel): String {
        val months = AppStrings.VEDIC_MONTHS[locale] ?: AppStrings.VEDIC_MONTHS["en"]!!
        return months[p.lunarMonthIndex]
    }

    fun vikramSamvatYear(year: Int, month: Int): Int =
        if (month >= 4) year + 57 else year + 56

    fun shakaSamvatYear(year: Int, month: Int, day: Int): Int =
        if (month > 3 || (month == 3 && day >= 22)) year - 78 else year - 79

    val vikramSamvatShortLabel: String get() = if (locale == "en") "VS" else "वि.सं."
    val shakaSamvatShortLabel: String get() = if (locale == "en") "SS" else "श.सं."

    fun vedicDateLine(p: PanchangModel): String {
        val tithi = tithiDisplay(p)
        val month = vedicMonthName(p)
        val vikram = numerals(vikramSamvatYear(p.date.year, p.date.month.ordinal + 1).toString())
        val shaka = numerals(shakaSamvatYear(p.date.year, p.date.month.ordinal + 1, p.date.day).toString())
        return "$tithi • $month • $vikramSamvatShortLabel $vikram • $shakaSamvatShortLabel $shaka"
    }

    // ── Numerals ──────────────────────────────────────────────────────────────

    fun numerals(input: String): String = if (locale != "sa") input else buildString {
        for (ch in input) {
            if (ch in '0'..'9') append(DEVA_DIGITS[ch - '0']) else append(ch)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    val untilLabel: String get() = strings["until"] ?: "until"
    val auspiciousLabel: String get() = strings["auspicious"] ?: "Auspicious"
    val inauspiciousLabel: String get() = strings["inauspicious"] ?: "Inauspicious"
}
