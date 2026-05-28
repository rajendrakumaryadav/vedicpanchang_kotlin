package `in`.vedicpanchang.app.data.datasource

import kotlin.math.floor

/**
 * Rule-based festival detection. All rules are based on tithi index (0-29),
 * Gregorian month (1-12), and sidereal solar/lunar longitude.
 *
 * Tithi index mapping:
 *  0 = Shukla Pratipada … 14 = Purnima
 * 15 = Krishna Pratipada … 29 = Amavasya
 */
object FestivalData {

    fun getFestivals(
        tithiIndex: Int,
        month: Int,           // 1–12 Gregorian
        sunLon: Double,       // sidereal
        moonLon: Double
    ): List<String> = RULES.filter { it.matches(tithiIndex, month, sunLon, moonLon) }.map { it.id }

    fun getLocalizedName(festivalId: String, locale: String): String = when (locale) {
        "hi" -> HI_NAMES[festivalId] ?: festivalId
        "sa" -> SA_NAMES[festivalId] ?: festivalId
        else -> festivalId
    }

    // ── Solar month helpers ───────────────────────────────────────────────────

    private fun solarMonthIndex(sunSiderealLon: Double): Int {
        val lon = ((sunSiderealLon % 360.0) + 360.0) % 360.0
        return floor(lon / 30.0).toInt() + 1  // 1=Mesha … 12=Meena
    }

    private fun isChaitraMonth(sunLon: Double) = solarMonthIndex(sunLon) == 12
    private fun isSharadMonth(sunLon: Double) = solarMonthIndex(sunLon) == 6

    // ── Festival rule DSL ─────────────────────────────────────────────────────

    private data class Rule(
        val id: String,
        val matches: (tithiIndex: Int, month: Int, sunLon: Double, moonLon: Double) -> Boolean
    )

    private val RULES = listOf(
        // ── Recurring (every lunar month) ─────────────────────────────────────
        Rule("Ekadashi")        { t, _, _, _ -> t == 10 || t == 25 },
        Rule("Purnima")         { t, _, _, _ -> t == 14 },
        Rule("Amavasya")        { t, _, _, _ -> t == 29 },
        Rule("Sankashti Chaturthi") { t, _, _, _ -> t == 18 },
        Rule("Pradosh Vrat")    { t, _, _, _ -> t == 12 || t == 27 },
        Rule("Masik Shivaratri") { t, _, _, _ -> t == 28 },

        // ── January / Magha ───────────────────────────────────────────────────
        Rule("Makar Sankranti") { _, m, sun, _ -> sun >= 270.0 && sun < 271.0 && m == 1 },
        Rule("Vasant Panchami") { t, m, _, _ -> t == 4 && (m == 1 || m == 2) },

        // ── February / Phalguna ───────────────────────────────────────────────
        Rule("Maha Shivaratri") { t, m, _, _ -> t == 28 && (m == 2 || m == 3) },

        // ── March / Chaitra ───────────────────────────────────────────────────
        Rule("Holika Dahan")    { t, m, _, _ -> t == 13 && m == 3 },
        Rule("Holi")            { t, m, _, _ -> t == 14 && m == 3 },
        Rule("Ugadi / Gudi Padwa") { t, _, sun, _ -> t == 0 && isChaitraMonth(sun) },
        Rule("Chaitra Navratri Begins") { t, _, sun, _ -> t == 0 && isChaitraMonth(sun) },
        Rule("Navratri Day 2 — Brahmacharini") { t, _, sun, _ -> t == 1 && isChaitraMonth(sun) },
        Rule("Navratri Day 3 — Chandraghanta") { t, _, sun, _ -> t == 2 && isChaitraMonth(sun) },
        Rule("Navratri Day 4 — Kushmanda")     { t, _, sun, _ -> t == 3 && isChaitraMonth(sun) },
        Rule("Navratri Day 5 — Skandamata")    { t, _, sun, _ -> t == 4 && isChaitraMonth(sun) },
        Rule("Navratri Day 6 — Katyayani")     { t, _, sun, _ -> t == 5 && isChaitraMonth(sun) },
        Rule("Ram Navami")      { t, _, sun, _ -> t == 8 && isChaitraMonth(sun) },
        Rule("Hanuman Jayanti") { t, _, sun, _ -> t == 14 && isChaitraMonth(sun) },

        // ── April-May / Vaishakha ─────────────────────────────────────────────
        Rule("Akshaya Tritiya") { t, _, sun, _ -> t == 2 && solarMonthIndex(sun) == 1 },
        Rule("Buddha Purnima")  { t, _, sun, _ -> t == 14 && solarMonthIndex(sun) == 1 },

        // ── July / Ashadha ────────────────────────────────────────────────────
        Rule("Guru Purnima")    { t, m, _, _ -> t == 14 && m == 7 },

        // ── July-August / Shravana ────────────────────────────────────────────
        Rule("Nag Panchami")    { t, m, _, _ -> t == 4 && (m == 7 || m == 8) },
        Rule("Raksha Bandhan")  { t, m, _, _ -> t == 14 && m == 8 },

        // ── August-September / Bhadrapada ─────────────────────────────────────
        Rule("Hartalika Teej")  { t, m, _, _ -> t == 2 && (m == 8 || m == 9) },
        Rule("Ganesh Chaturthi") { t, m, _, _ -> t == 3 && (m == 8 || m == 9) },
        Rule("Anant Chaturdashi") { t, m, _, _ -> t == 13 && (m == 8 || m == 9) },
        Rule("Krishna Janmashtami") { t, m, _, _ -> t == 22 && (m == 8 || m == 9) },

        // ── September-October / Ashwin (Sharad Navratri) ─────────────────────
        Rule("Sharad Navratri Begins") { t, _, sun, _ -> t == 0 && isSharadMonth(sun) },
        Rule("Navratri Day 2 — Brahmacharini") { t, _, sun, _ -> t == 1 && isSharadMonth(sun) },
        Rule("Navratri Day 3 — Chandraghanta") { t, _, sun, _ -> t == 2 && isSharadMonth(sun) },
        Rule("Navratri Day 4 — Kushmanda")     { t, _, sun, _ -> t == 3 && isSharadMonth(sun) },
        Rule("Navratri Day 5 — Skandamata")    { t, _, sun, _ -> t == 4 && isSharadMonth(sun) },
        Rule("Navratri Day 6 — Katyayani")     { t, _, sun, _ -> t == 5 && isSharadMonth(sun) },
        Rule("Durga Saptami")   { t, _, sun, _ -> t == 6 && isSharadMonth(sun) },
        Rule("Durga Maha Ashtami") { t, _, sun, _ -> t == 7 && isSharadMonth(sun) },
        Rule("Durga Maha Navami")  { t, _, sun, _ -> t == 8 && isSharadMonth(sun) },
        Rule("Vijayadashami (Dussehra)") { t, _, sun, _ -> t == 9 && isSharadMonth(sun) },
        Rule("Sharad Purnima")  { t, m, _, _ -> t == 14 && (m == 9 || m == 10) },
        Rule("Karva Chauth")    { t, m, _, _ -> t == 18 && (m == 10 || m == 11) },

        // ── October-November / Kartik ─────────────────────────────────────────
        Rule("Dhanteras")       { t, m, _, _ -> t == 27 && (m == 10 || m == 11) },
        Rule("Naraka Chaturdashi") { t, m, _, _ -> t == 28 && (m == 10 || m == 11) },
        Rule("Diwali")          { t, m, _, _ -> t == 29 && (m == 10 || m == 11) },
        Rule("Govardhan Puja")  { t, m, _, _ -> t == 0 && (m == 10 || m == 11) },
        Rule("Bhai Dooj")       { t, m, _, _ -> t == 1 && (m == 10 || m == 11) },
        Rule("Chhath Puja")     { t, m, _, _ -> t == 5 && (m == 10 || m == 11) },
        Rule("Dev Uthani Ekadashi") { t, m, _, _ -> t == 10 && (m == 10 || m == 11) },
        Rule("Kartik Purnima")  { t, m, _, _ -> t == 14 && (m == 11 || m == 12) },
        Rule("Rama Ekadashi")   { t, m, _, _ -> t == 25 && (m == 10 || m == 11) }
    )

    // ── Localized names ───────────────────────────────────────────────────────

    private val HI_NAMES = mapOf(
        "Ekadashi" to "एकादशी", "Purnima" to "पूर्णिमा", "Amavasya" to "अमावस्या",
        "Sankashti Chaturthi" to "संकष्टी चतुर्थी", "Pradosh Vrat" to "प्रदोष व्रत",
        "Masik Shivaratri" to "मासिक शिवरात्रि", "Akshaya Tritiya" to "अक्षय तृतीया",
        "Holi" to "होली", "Holika Dahan" to "होलिका दहन", "Diwali" to "दीपावली",
        "Dhanteras" to "धनतेरस", "Naraka Chaturdashi" to "नरक चतुर्दशी",
        "Govardhan Puja" to "गोवर्धन पूजा", "Bhai Dooj" to "भाई दूज",
        "Ugadi / Gudi Padwa" to "उगादि / गुड़ी पड़वा", "Ram Navami" to "राम नवमी",
        "Hanuman Jayanti" to "हनुमान जयंती", "Krishna Janmashtami" to "कृष्ण जन्माष्टमी",
        "Ganesh Chaturthi" to "गणेश चतुर्थी", "Makar Sankranti" to "मकर संक्रांति",
        "Guru Purnima" to "गुरु पूर्णिमा", "Raksha Bandhan" to "रक्षाबंधन",
        "Maha Shivaratri" to "महा शिवरात्रि", "Vasant Panchami" to "वसंत पंचमी",
        "Chaitra Navratri Begins" to "चैत्र नवरात्रि आरंभ",
        "Sharad Navratri Begins" to "शारदीय नवरात्रि आरंभ",
        "Navratri Day 2 — Brahmacharini" to "नवरात्रि दिन 2 — ब्रह्मचारिणी",
        "Navratri Day 3 — Chandraghanta" to "नवरात्रि दिन 3 — चंद्रघंटा",
        "Navratri Day 4 — Kushmanda" to "नवरात्रि दिन 4 — कूष्माण्डा",
        "Navratri Day 5 — Skandamata" to "नवरात्रि दिन 5 — स्कंदमाता",
        "Navratri Day 6 — Katyayani" to "नवरात्रि दिन 6 — कात्यायनी",
        "Durga Saptami" to "दुर्गा सप्तमी", "Durga Maha Ashtami" to "दुर्गा महा अष्टमी",
        "Durga Maha Navami" to "दुर्गा महा नवमी",
        "Vijayadashami (Dussehra)" to "विजयादशमी (दशहरा)",
        "Karva Chauth" to "करवा चौथ", "Chhath Puja" to "छठ पूजा",
        "Sharad Purnima" to "शरद पूर्णिमा", "Buddha Purnima" to "बुद्ध पूर्णिमा",
        "Nag Panchami" to "नाग पंचमी", "Hartalika Teej" to "हरतालिका तीज",
        "Anant Chaturdashi" to "अनंत चतुर्दशी", "Kartik Purnima" to "कार्तिक पूर्णिमा",
        "Dev Uthani Ekadashi" to "देव उठनी एकादशी", "Rama Ekadashi" to "रामा एकादशी"
    )

    private val SA_NAMES = mapOf(
        "Ekadashi" to "एकादशी", "Purnima" to "पूर्णिमा", "Amavasya" to "अमावस्या",
        "Sankashti Chaturthi" to "सङ्कष्टचतुर्थी", "Pradosh Vrat" to "प्रदोषव्रतम्",
        "Masik Shivaratri" to "मासिकशिवरात्रिः", "Akshaya Tritiya" to "अक्षयतृतीया",
        "Holi" to "होलिकोत्सवः", "Holika Dahan" to "होलिकादहनम्", "Diwali" to "दीपावलिः",
        "Dhanteras" to "धनत्रयोदशी", "Naraka Chaturdashi" to "नरकचतुर्दशी",
        "Govardhan Puja" to "गोवर्धनपूजा", "Bhai Dooj" to "भ्रातृद्वितीया",
        "Ugadi / Gudi Padwa" to "युगादिः", "Ram Navami" to "रामनवमी",
        "Hanuman Jayanti" to "हनूमज्जयन्ती", "Krishna Janmashtami" to "श्रीकृष्णजन्माष्टमी",
        "Ganesh Chaturthi" to "गणेशचतुर्थी", "Makar Sankranti" to "मकरसङ्क्रान्तिः",
        "Guru Purnima" to "गुरुपूर्णिमा", "Raksha Bandhan" to "रक्षाबन्धनम्",
        "Maha Shivaratri" to "महाशिवरात्रिः", "Vasant Panchami" to "वसन्तपञ्चमी",
        "Chaitra Navratri Begins" to "चैत्रनवरात्र्यारम्भः",
        "Sharad Navratri Begins" to "शारदनवरात्र्यारम्भः",
        "Navratri Day 2 — Brahmacharini" to "नवरात्रि दिनम् 2 — ब्रह्मचारिणी",
        "Navratri Day 3 — Chandraghanta" to "नवरात्रि दिनम् 3 — चन्द्रघण्टा",
        "Navratri Day 4 — Kushmanda" to "नवरात्रि दिनम् 4 — कूष्माण्डा",
        "Navratri Day 5 — Skandamata" to "नवरात्रि दिनम् 5 — स्कन्दमाता",
        "Navratri Day 6 — Katyayani" to "नवरात्रि दिनम् 6 — कात्यायनी",
        "Durga Saptami" to "दुर्गासप्तमी", "Durga Maha Ashtami" to "दुर्गामहाष्टमी",
        "Durga Maha Navami" to "दुर्गामहानवमी",
        "Vijayadashami (Dussehra)" to "विजयादशमी (दशहरा)",
        "Karva Chauth" to "करकचतुर्थी", "Chhath Puja" to "षष्ठीपूजा",
        "Sharad Purnima" to "शरत्पूर्णिमा", "Buddha Purnima" to "बुद्धपूर्णिमा",
        "Nag Panchami" to "नागपञ्चमी", "Hartalika Teej" to "हरतालिकातृतीया",
        "Anant Chaturdashi" to "अनन्तचतुर्दशी", "Kartik Purnima" to "कार्तिकपूर्णिमा",
        "Dev Uthani Ekadashi" to "देवोत्थानैकादशी", "Rama Ekadashi" to "रामैकादशी"
    )
}
