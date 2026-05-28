package `in`.vedicpanchang.app.l10n

import `in`.vedicpanchang.app.data.model.PlanetData

private val EN_SIGNS = listOf(
    "Aries","Taurus","Gemini","Cancer","Leo","Virgo",
    "Libra","Scorpio","Sagittarius","Capricorn","Aquarius","Pisces"
)

private val SIGN_INDEX = EN_SIGNS.mapIndexed { i, s -> s to i }.toMap()

private val NAKSHATRA_INDEX = mapOf(
    "Ashwini" to 0, "Bharani" to 1, "Krittika" to 2, "Rohini" to 3,
    "Mrigashira" to 4, "Ardra" to 5, "Punarvasu" to 6, "Pushya" to 7,
    "Ashlesha" to 8, "Magha" to 9, "Purva Phalguni" to 10, "Uttara Phalguni" to 11,
    "Hasta" to 12, "Chitra" to 13, "Swati" to 14, "Vishakha" to 15,
    "Anuradha" to 16, "Jyeshtha" to 17, "Mula" to 18, "Purva Ashadha" to 19,
    "Uttara Ashadha" to 20, "Shravana" to 21, "Dhanishtha" to 22,
    "Shatabhisha" to 23, "Purva Bhadrapada" to 24, "Uttara Bhadrapada" to 25, "Revati" to 26
)

/**
 * Localizes Kundali-specific data values (sign names, planet names, interpretations).
 * Equivalent of horoscope_localizer.dart.
 */
class HoroscopeLocalizer(val locale: String) {

    private val strings = AppStrings.of(locale)

    // ── Sign (Rashi) names ────────────────────────────────────────────────────

    fun signName(index: Int): String {
        val clamped = index.coerceIn(0, 11)
        if (locale == "en") return EN_SIGNS[clamped]
        return AppStrings.SIGN_NAMES[locale]?.getOrNull(clamped) ?: EN_SIGNS[clamped]
    }

    fun signNameFromEnglish(english: String): String {
        if (locale == "en") return english
        val idx = SIGN_INDEX[english] ?: return english
        return signName(idx)
    }

    // ── Planet names ──────────────────────────────────────────────────────────

    fun planetName(english: String): String {
        if (locale == "en") return english
        return AppStrings.PLANET_NAMES[locale]?.get(english) ?: english
    }

    // ── Nakshatra names ───────────────────────────────────────────────────────

    fun nakshatraName(english: String): String {
        if (locale == "en") return english
        val idx = NAKSHATRA_INDEX[english] ?: return english
        return AppStrings.NAKSHATRA_NAMES[locale]?.getOrNull(idx) ?: english
    }

    // ── Planet interpretation notes ───────────────────────────────────────────

    fun planetNote(p: PlanetData): String = when (locale) {
        "hi" -> planetNoteHi(p)
        "sa" -> planetNoteSa(p)
        else -> planetNoteEn(p)
    }

    fun planetPositionLabel(p: PlanetData): String {
        val planet = planetName(p.name)
        val sign = signNameFromEnglish(p.signName)
        val houseLabel = strings["house_col"] ?: "H"
        val retroLabel = if (p.isRetrograde) " ${strings["retrograde"] ?: "(R)"}" else ""
        return "$planet — $sign ($houseLabel ${p.houseNumber})$retroLabel"
    }

    // ── English notes ─────────────────────────────────────────────────────────

    private fun planetNoteEn(p: PlanetData): String = when (p.name) {
        "Sun" -> sunNoteEn(p)
        "Moon" -> moonNoteEn(p)
        "Mars" -> "Drives energy, courage, and assertion through the themes of ${p.signName}." +
                  if (p.isRetrograde) " Retrograde — energy turns inward." else ""
        "Mercury" -> "Shapes intellect, communication style, and analytical approach." +
                     if (p.isRetrograde) " Retrograde — reflection over expression." else ""
        "Jupiter" -> "Expands wisdom, fortune, and spiritual growth in house ${p.houseNumber}." +
                     if (p.isRetrograde) " Retrograde — inner spiritual growth." else ""
        "Venus" -> "Governs love, beauty, and relationships through ${p.signName}." +
                   if (p.isRetrograde) " Retrograde — revisiting relationship lessons." else ""
        "Saturn" -> "Teaches discipline, patience, and karmic lessons in house ${p.houseNumber}." +
                    if (p.isRetrograde) " Retrograde — deepened karmic review." else ""
        "Rahu" -> "Amplifies themes of ${p.signName}; karmic desire, unconventional experiences in house ${p.houseNumber}."
        "Ketu" -> "Spiritual detachment; past-life wisdom expressed through house ${p.houseNumber}."
        else -> ""
    }

    private fun sunNoteEn(p: PlanetData) = mapOf(
        "Aries" to "Pioneer spirit, bold leadership, high energy.",
        "Taurus" to "Steady, reliable, love of beauty and comfort.",
        "Gemini" to "Intellectual, communicative, adaptable.",
        "Cancer" to "Intuitive, nurturing, emotionally sensitive.",
        "Leo" to "Natural authority; Sun is exalted here — confident, creative.",
        "Virgo" to "Analytical, service-oriented, detail-focused.",
        "Libra" to "Diplomatic, justice-seeking, relationship-oriented.",
        "Scorpio" to "Intense, transformative, penetrating insight.",
        "Sagittarius" to "Philosophical, adventurous, truth-seeking.",
        "Capricorn" to "Disciplined, ambitious, career-focused.",
        "Aquarius" to "Humanitarian, innovative, independent.",
        "Pisces" to "Compassionate, intuitive, spiritually inclined."
    )[p.signName] ?: ""

    private fun moonNoteEn(p: PlanetData) = mapOf(
        "Aries" to "Emotionally impulsive, pioneering feelings.",
        "Taurus" to "Moon exalted — emotionally stable, comfort-loving.",
        "Gemini" to "Curious mind, changeable moods.",
        "Cancer" to "Moon at home — deeply nurturing, intuitive.",
        "Leo" to "Warm-hearted, proud, generous emotions.",
        "Virgo" to "Analytical feelings, health-conscious.",
        "Libra" to "Harmonious, people-pleasing emotions.",
        "Scorpio" to "Moon debilitated — intense, secretive emotions.",
        "Sagittarius" to "Optimistic, philosophical feelings.",
        "Capricorn" to "Reserved, disciplined emotions.",
        "Aquarius" to "Detached, humanitarian feelings.",
        "Pisces" to "Deeply empathic, dreamy, spiritual."
    )[p.signName] ?: ""

    // ── Hindi notes ───────────────────────────────────────────────────────────

    private fun planetNoteHi(p: PlanetData): String {
        val sign = AppStrings.SIGN_NAMES["hi"]?.getOrNull(SIGN_INDEX[p.signName] ?: 0) ?: p.signName
        val retro = if (p.isRetrograde) " वक्री — ऊर्जा अंतर्मुखी।" else ""
        return when (p.name) {
            "Sun" -> sunNoteHi(p)
            "Moon" -> moonNoteHi(p)
            "Mars" -> "$sign राशि में साहस, ऊर्जा और दृढ़ता को प्रेरित करते हैं।$retro"
            "Mercury" -> "बुद्धि, संचार और विश्लेषण क्षमता को प्रभावित करते हैं।$retro"
            "Jupiter" -> "भाव ${p.houseNumber} में ज्ञान, सौभाग्य और अध्यात्म का विस्तार।$retro"
            "Venus" -> "$sign राशि में प्रेम, सौंदर्य और संबंधों पर शासन।$retro"
            "Saturn" -> "भाव ${p.houseNumber} में अनुशासन, धैर्य और कार्मिक पाठ सिखाते हैं।$retro"
            "Rahu" -> "$sign राशि के विषयों को प्रबलित करते हैं; भाव ${p.houseNumber} में कर्मिक इच्छाएं।"
            "Ketu" -> "आध्यात्मिक विरक्ति; भाव ${p.houseNumber} में पूर्वजन्म का ज्ञान।"
            else -> ""
        }
    }

    private fun sunNoteHi(p: PlanetData) = mapOf(
        "Aries" to "अग्रणी भावना, साहसी नेतृत्व, उच्च ऊर्जा।",
        "Taurus" to "स्थिर, विश्वसनीय, सौंदर्य और आराम का प्रेम।",
        "Gemini" to "बौद्धिक, संचारी, अनुकूलनशील।",
        "Cancer" to "अंतर्ज्ञानी, पालन-पोषण, भावनात्मक संवेदनशीलता।",
        "Leo" to "प्राकृतिक प्रभाव; सूर्य उच्च — आत्मविश्वासी, रचनात्मक।",
        "Virgo" to "विश्लेषणात्मक, सेवाभावी, विस्तार पर ध्यान।",
        "Libra" to "कूटनीतिक, न्यायप्रिय, संबंध-उन्मुख।",
        "Scorpio" to "तीव्र, परिवर्तनकारी, गहरी अंतर्दृष्टि।",
        "Sagittarius" to "दार्शनिक, साहसी, सत्य की खोज।",
        "Capricorn" to "अनुशासित, महत्वाकांक्षी, कैरियर-केंद्रित।",
        "Aquarius" to "मानवतावादी, नवाचारी, स्वतंत्र।",
        "Pisces" to "दयालु, अंतर्ज्ञानी, आध्यात्मिक रुझान।"
    )[p.signName] ?: ""

    private fun moonNoteHi(p: PlanetData) = mapOf(
        "Aries" to "भावनात्मक रूप से आवेगी, अग्रणी भावनाएं।",
        "Taurus" to "चंद्र उच्च — भावनात्मक स्थिरता, आराम-प्रेम।",
        "Gemini" to "जिज्ञासु मन, परिवर्तनशील मनोदशा।",
        "Cancer" to "चंद्र स्वगृह — गहन पालन-पोषण, अंतर्ज्ञानी।",
        "Leo" to "उदार हृदय, गर्वित, उदार भावनाएं।",
        "Virgo" to "विश्लेषणात्मक भावनाएं, स्वास्थ्य-सचेत।",
        "Libra" to "सामंजस्यपूर्ण, लोगों को खुश करने वाली भावनाएं।",
        "Scorpio" to "चंद्र नीच — तीव्र, गुप्त भावनाएं।",
        "Sagittarius" to "आशावादी, दार्शनिक भावनाएं।",
        "Capricorn" to "संयमित, अनुशासित भावनाएं।",
        "Aquarius" to "विरक्त, मानवतावादी भावनाएं।",
        "Pisces" to "गहरी सहानुभूति, स्वप्निल, आध्यात्मिक।"
    )[p.signName] ?: ""

    // ── Sanskrit notes ────────────────────────────────────────────────────────

    private fun planetNoteSa(p: PlanetData): String {
        val sign = AppStrings.SIGN_NAMES["sa"]?.getOrNull(SIGN_INDEX[p.signName] ?: 0) ?: p.signName
        val retro = if (p.isRetrograde) " वक्री — ऊर्जा अन्तर्मुखी।" else ""
        return when (p.name) {
            "Sun" -> sunNoteSa(p)
            "Moon" -> moonNoteSa(p)
            "Mars" -> "$sign राशौ साहसं, ऊर्जा, दृढ़ता च प्रेरयति।$retro"
            "Mercury" -> "बुद्धिं, संचारं, विश्लेषणशक्तिं च प्रभावयति।$retro"
            "Jupiter" -> "भाव ${p.houseNumber} ज्ञानं, सौभाग्यं, आध्यात्मिकवृद्धिं च विस्तारयति।$retro"
            "Venus" -> "$sign राशौ प्रेम-सौन्दर्य-सम्बन्धान् नियच्छति।$retro"
            "Saturn" -> "भाव ${p.houseNumber} अनुशासनं, धैर्यं, कार्मिकपाठान् च शिक्षयति।$retro"
            "Rahu" -> "$sign राशेः विषयान् प्रबलयति; भाव ${p.houseNumber} कार्मिकेच्छाः।"
            "Ketu" -> "आध्यात्मिकविरक्तिः; भाव ${p.houseNumber} पूर्वजन्मज्ञानम्।"
            else -> ""
        }
    }

    private fun sunNoteSa(p: PlanetData) = mapOf(
        "Aries" to "अग्रणीभावः, साहसिकनेतृत्वम्, उच्चोर्जा।",
        "Taurus" to "स्थिरः, विश्वसनीयः, सौन्दर्यप्रेम।",
        "Gemini" to "बौद्धिकः, संचारी, अनुकूलनशीलः।",
        "Cancer" to "अन्तर्ज्ञानी, पोषणशीलः, भावनासंवेदनशीलः।",
        "Leo" to "स्वाभाविकप्रभावः; सूर्य उच्च — आत्मविश्वासी, सृजनशीलः।",
        "Virgo" to "विश्लेषणात्मकः, सेवाभावी, विस्तारधर्मी।",
        "Libra" to "राजनैतिकः, न्यायप्रियः, सम्बन्धकेन्द्रितः।",
        "Scorpio" to "तीव्रः, परिवर्तनकारी, गहनान्तर्दृष्टिः।",
        "Sagittarius" to "दार्शनिकः, साहसिकः, सत्यान्वेषी।",
        "Capricorn" to "अनुशासितः, महत्त्वाकांक्षी, कैरियरकेन्द्रितः।",
        "Aquarius" to "मानवतावादी, नवाचारी, स्वतन्त्रः।",
        "Pisces" to "करुणामयः, अन्तर्ज्ञानी, आध्यात्मिकरुचिः।"
    )[p.signName] ?: ""

    private fun moonNoteSa(p: PlanetData) = mapOf(
        "Aries" to "भावनात्मकावेगी, अग्रणीभावनाः।",
        "Taurus" to "चन्द्र उच्च — भावनास्थिरः, आरामप्रियः।",
        "Gemini" to "जिज्ञासुमनः, परिवर्तनशीलमनःस्थितिः।",
        "Cancer" to "चन्द्र स्वगृह — गहनपोषणशीलः, अन्तर्ज्ञानी।",
        "Leo" to "उदारहृदयः, गर्वितः, उदारभावनाः।",
        "Virgo" to "विश्लेषणात्मकभावनाः, स्वास्थ्यसचेतः।",
        "Libra" to "सामञ्जस्यपूर्णः, लोकप्रियभावनाः।",
        "Scorpio" to "चन्द्र नीच — तीव्र, गुप्तभावनाः।",
        "Sagittarius" to "आशावादी, दार्शनिकभावनाः।",
        "Capricorn" to "संयमित, अनुशासितभावनाः।",
        "Aquarius" to "विरक्त, मानवतावादीभावनाः।",
        "Pisces" to "गहनसहानुभूतिः, स्वप्निलः, आध्यात्मिकः।"
    )[p.signName] ?: ""
}
