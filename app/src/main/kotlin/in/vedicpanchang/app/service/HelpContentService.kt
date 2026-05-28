package `in`.vedicpanchang.app.service

import `in`.vedicpanchang.app.data.model.HelpEntry
import `in`.vedicpanchang.app.data.model.HelpSection

/** Localized help content for app concepts and calculations. */
object HelpContentService {
    fun sectionsForLocale(locale: String): List<HelpSection> = when (locale) {
        "hi" -> HI_SECTIONS
        "sa" -> SA_SECTIONS
        else -> EN_SECTIONS
    }

    // ── English ────────────────────────────────────────────────────────────────

    private val EN_SECTIONS = listOf(
        HelpSection(
            icon = "📖",
            title = "About this app",
            intro = "Vedic Panchang computes Hindu calendar data for any date and location using built-in astronomy (Jean Meeus algorithms). No internet or external API is needed — all calculations happen on your device.",
            entries = emptyList()
        ),
        HelpSection(
            icon = "🕉️",
            title = "Core Panchang — the five limbs",
            intro = "Panchang (पञ्चाङ्ग) literally means \"five limbs\". These five elements together describe the quality of any given moment in Vedic time.",
            entries = listOf(
                HelpEntry(
                    icon = "🌙",
                    parameter = "Tithi — Lunar Day",
                    meaning = "A tithi is a lunar day defined by the angular gap between the Moon and Sun. Each 12° of separation equals one tithi. There are 30 tithis in a lunar month — 15 in the bright fortnight (Shukla Paksha) and 15 in the dark fortnight (Krishna Paksha). Tithis govern fasting days, festivals, and the auspiciousness of activities.",
                    calculation = "D = (Moon longitude − Sun longitude) mod 360. Tithi number = ⌊D ÷ 12⌋ + 1. The start and end times are found by solving when D crosses the next multiple of 12°."
                ),
                HelpEntry(
                    icon = "⭐",
                    parameter = "Nakshatra — Lunar Mansion",
                    meaning = "The sky is divided into 27 lunar mansions (nakshatras), each spanning 13°20'. The nakshatra occupied by the Moon at any moment describes the quality of that time. Your birth nakshatra (Janma Nakshatra) determines your natal star and is central to muhurta selection and compatibility matching.",
                    calculation = "Nakshatra = ⌊Moon sidereal longitude ÷ 13.333…⌋ + 1. Start/end times are the moments the Moon crosses each 13°20' boundary."
                ),
                HelpEntry(
                    icon = "☯️",
                    parameter = "Yoga — Combined Influence",
                    meaning = "There are 27 yogas formed by adding the Sun and Moon longitudes. They range from highly auspicious (Siddha, Shubha, Amrita) to inauspicious (Vyatipata, Vaidhriti). The yoga of a day reflects the combined solar-lunar energy and helps assess the overall spiritual quality of the day.",
                    calculation = "Sum = (Sun longitude + Moon longitude) mod 360. Yoga = ⌊Sum ÷ 13.333…⌋ + 1."
                ),
                HelpEntry(
                    icon = "🔀",
                    parameter = "Karana — Half Tithi",
                    meaning = "A karana is half of a tithi (6°). There are 11 karanas — 4 fixed (Shakuni, Chatushpada, Naga, Kimstughna) and 7 movable (Bava, Balava, Kaulava, Taitila, Gara, Vanija, Vishti). The current karana and the one after (shown as \"→ next\") are used for fine-grained muhurta selection within a tithi.",
                    calculation = "D = (Moon longitude − Sun longitude) mod 360. Karana index = ⌊D ÷ 6⌋, then mapped to the classical fixed/movable karana cycle."
                ),
                HelpEntry(
                    icon = "📅",
                    parameter = "Vaar — Weekday",
                    meaning = "Each weekday is ruled by a planet: Sun (Sunday), Moon (Monday), Mars (Tuesday), Mercury (Wednesday), Jupiter (Thursday), Venus (Friday), Saturn (Saturday). The ruling planet colors the day's overall energy and influences which activities are favored.",
                    calculation = "Determined from the civil weekday of the local date after timezone conversion. No astronomical computation needed."
                )
            )
        ),
        HelpSection(
            icon = "🌅",
            title = "Sun, Moon & time periods",
            intro = "All time-based calculations anchor on the local sunrise and sunset for your exact coordinates and date. Changing your location updates all periods automatically.",
            entries = listOf(
                HelpEntry(
                    icon = "🌅",
                    parameter = "Sunrise & Sunset",
                    meaning = "The apparent rise and set times of the Sun at your location. These are the master anchors for all Panchang time periods — Rahu Kaal, Muhurtas, and inauspicious windows all derive from the day's length.",
                    calculation = "Computed using solar position equations (Jean Meeus) with atmospheric refraction (+0.5667°) and horizon corrections for your latitude and longitude."
                ),
                HelpEntry(
                    icon = "🌕",
                    parameter = "Moonrise & Moonset",
                    meaning = "Local rise and set times of the Moon. Relevant for nighttime observances, tithi transitions, and determining when to break a fast. The Moon moves fast enough (~13°/day) that moonrise shifts by ~50 minutes each day.",
                    calculation = "Sampled lunar positions over the 24-hour day; horizon-crossing time is solved by interpolation between samples."
                ),
                HelpEntry(
                    icon = "⚠️",
                    parameter = "Rahu Kaal",
                    meaning = "A ~90-minute daily period ruled by Rahu (the North lunar node). Traditionally considered the most inauspicious window of the day — avoid starting new ventures, travel, signing contracts, or important decisions. The specific block differs by weekday.",
                    calculation = "Daytime (sunrise→sunset) is split into 8 equal blocks. The weekday map selects which block is Rahu Kaal: Sun=8th, Mon=2nd, Tue=7th, Wed=5th, Thu=6th, Fri=4th, Sat=3rd."
                ),
                HelpEntry(
                    icon = "💀",
                    parameter = "Yamaganda",
                    meaning = "A daytime period ruled by Yama, the deity of death. Second in severity to Rahu Kaal among inauspicious periods. Avoid auspicious rites, ceremonies, and new beginnings during this window.",
                    calculation = "Same 8-block split as Rahu Kaal. Weekday map: Sun=5th, Mon=4th, Tue=3rd, Wed=2nd, Thu=1st, Fri=8th, Sat=7th."
                ),
                HelpEntry(
                    icon = "🪐",
                    parameter = "Gulika Kaal",
                    meaning = "The period of Gulika (Manda), son of Saturn. Less severe than Rahu Kaal but still traditionally avoided for auspicious activities. Associated with obstacles and delays.",
                    calculation = "Same 8-block method. Weekday map: Sun=6th, Mon=5th, Tue=4th, Wed=3rd, Thu=2nd, Fri=1st, Sat=8th."
                ),
                HelpEntry(
                    icon = "🙏",
                    parameter = "Brahma Muhurta",
                    meaning = "The \"Hour of Brahma\" — approximately 96 minutes before sunrise. The finest time of day for meditation, yoga, pranayama, and scriptural study. The mind is naturally calm, and nature is quiet. Waking during Brahma Muhurta is recommended in classical Ayurveda and Yoga texts.",
                    calculation = "Fixed as a pre-dawn window ending at sunrise, calculated from the day's exact sunrise time for your location."
                ),
                HelpEntry(
                    icon = "⚡",
                    parameter = "Abhijit Muhurta",
                    meaning = "A ~48-minute window centered on local solar noon. One of the most auspicious muhurtas — excellent for starting new ventures, important decisions, signing agreements, or any activity where success is desired. It is the 8th of the 15 daytime muhurtas.",
                    calculation = "Daytime is divided into 15 equal muhurtas; Abhijit is centered around the midday muhurta. Duration = daytime ÷ 15."
                ),
                HelpEntry(
                    icon = "🏆",
                    parameter = "Vijaya Muhurta",
                    meaning = "The \"victory period\" in the late afternoon. Favored for competitive activities, challenging tasks, and overcoming obstacles. Traditional texts recommend it for battles, debates, and ambitious beginnings.",
                    calculation = "Corresponds to the 14th segment of the 15-part daytime muhurta division."
                ),
                HelpEntry(
                    icon = "🌆",
                    parameter = "Godhuli Muhurta",
                    meaning = "The \"cow-dust hour\" near sunset — named for the golden dust raised by cattle returning home. Auspicious for sacred rituals, griha-pravesh (entering a new home), marriage ceremonies, and beginning new ventures. Short but highly regarded.",
                    calculation = "A symmetric window centered on the exact sunset time."
                ),
                HelpEntry(
                    icon = "🕉️",
                    parameter = "Day Muhurtas (15)",
                    meaning = "The full daytime from sunrise to sunset is divided into 15 equal segments, each named after a deity: Rudra, Ahi, Mitra, Pitru, Vasu, Varaha, Vishwadeva, Vidhi, Satamukhi, Puruhuta, Vahni, Naktanchara, Varuna, Aryama, and Bhaga. Used in traditional scheduling for specific activities.",
                    calculation = "MuhurtaLength = (Sunset − Sunrise) ÷ 15. Slot n spans [Sunrise + (n−1)×L, Sunrise + n×L]."
                )
            )
        ),
        HelpSection(
            icon = "📐",
            title = "Formula reference",
            intro = "Core inputs: latitude (φ), longitude (λ), local date and timezone. Key symbols: Ls = Sun sidereal longitude, Lm = Moon sidereal longitude, JD = Julian Day number.",
            entries = listOf(
                HelpEntry(
                    icon = "🌙",
                    parameter = "Tithi formula",
                    meaning = "Angular lunar-day index. Range: 1–30.",
                    calculation = "D = (Lm − Ls) mod 360; Tithi = ⌊D ÷ 12⌋ + 1. One tithi = exactly 12°."
                ),
                HelpEntry(
                    icon = "⭐",
                    parameter = "Nakshatra formula",
                    meaning = "Moon mansion index. Range: 1–27.",
                    calculation = "Nakshatra = ⌊(Lm mod 360) ÷ (360 ÷ 27)⌋ + 1. Each segment = 13°20'."
                ),
                HelpEntry(
                    icon = "☯️",
                    parameter = "Yoga formula",
                    meaning = "Combined solar-lunar index. Range: 1–27.",
                    calculation = "Yoga = ⌊((Ls + Lm) mod 360) ÷ (360 ÷ 27)⌋ + 1."
                ),
                HelpEntry(
                    icon = "🔀",
                    parameter = "Karana formula",
                    meaning = "Half-tithi index. One karana = 6°.",
                    calculation = "D = (Lm − Ls) mod 360; Karana index = ⌊D ÷ 6⌋, then mapped to the 11-karana cycle."
                ),
                HelpEntry(
                    icon = "⚠️",
                    parameter = "Inauspicious-period formula",
                    meaning = "Rahu Kaal, Yamaganda, and Gulika Kaal block selection.",
                    calculation = "BlockLength = (Sunset − Sunrise) ÷ 8. A weekday-to-block map picks which 1-of-8 slot is used for each period."
                ),
                HelpEntry(
                    icon = "⏱️",
                    parameter = "Muhurta segmentation",
                    meaning = "Divides daytime into 15 equal muhurta slots.",
                    calculation = "L = (Sunset − Sunrise) ÷ 15. Slot n = [Sunrise + (n−1)×L, Sunrise + n×L]."
                )
            )
        ),
        HelpSection(
            icon = "🔭",
            title = "Astronomical values & festivals",
            intro = "Raw astronomical data and festival detection logic used internally by the app.",
            entries = listOf(
                HelpEntry(
                    icon = "☀️",
                    parameter = "Sun & Moon Longitude",
                    meaning = "Sidereal (Nirāyana) longitudes — referenced to the fixed stars rather than the tropical (seasonal) zodiac. The app applies a Lahiri-style ayanāmsha (~23.85° for current epoch) to convert from tropical to sidereal. These are the root inputs for Tithi, Nakshatra, and Yoga.",
                    calculation = "Computed using Jean Meeus planetary position algorithms evaluated at the Julian Day for the observation time."
                ),
                HelpEntry(
                    icon = "📆",
                    parameter = "Julian Day (JD)",
                    meaning = "A continuous day-count from noon on 1 January 4713 BCE. Astronomers use JD to avoid calendar ambiguities across different calendar systems. The fractional part represents the time within the day.",
                    calculation = "JD = 367×Y − ⌊7×(Y+⌊(M+9)÷12⌋)÷4⌋ + ⌊275×M÷9⌋ + D + 1721013.5 + UT÷24 (Meeus formula)."
                ),
                HelpEntry(
                    icon = "🎉",
                    parameter = "Festival detection",
                    meaning = "Festivals are identified by matching the current tithi, lunar month, paksha, and special conditions (e.g., specific nakshatra or weekday combinations) against a built-in rule table. No external data source is used.",
                    calculation = "Rule-based matching: e.g., Diwali = Krishna Paksha Amavasya of Kartika month; Holi = Phalguna Purnima. Combined conditions are evaluated per-day."
                )
            )
        )
    )

    // ── Hindi ──────────────────────────────────────────────────────────────────

    private val HI_SECTIONS = listOf(
        HelpSection(
            icon = "📖",
            title = "ऐप के बारे में",
            intro = "वैदिक पंचांग ऐप चुनी गई तिथि और स्थान के अनुसार Jean Meeus के खगोलीय सूत्रों से सभी पंचांग मान डिवाइस पर स्वयं गणना करता है। इंटरनेट या बाहरी API की आवश्यकता नहीं है।",
            entries = emptyList()
        ),
        HelpSection(
            icon = "🕉️",
            title = "मुख्य पंचांग — पाँच अंग",
            intro = "पंचांग का अर्थ है \"पाँच अंग\"। ये पाँच तत्व मिलकर किसी भी क्षण की वैदिक समय-गुणवत्ता का पूर्ण वर्णन करते हैं।",
            entries = listOf(
                HelpEntry(
                    icon = "🌙",
                    parameter = "तिथि — चंद्र दिवस",
                    meaning = "तिथि वह चंद्र दिवस है जो सूर्य और चंद्र के कोणीय अंतर से निर्धारित होती है। प्रत्येक 12° अंतर एक तिथि बनाता है। चंद्र मास में 30 तिथियाँ होती हैं — 15 शुक्ल पक्ष में और 15 कृष्ण पक्ष में। तिथि से व्रत, त्योहार और शुभ कार्यों का निर्धारण होता है।",
                    calculation = "D = (चंद्र देशांतर − सूर्य देशांतर) mod 360। तिथि = ⌊D ÷ 12⌋ + 1। जब D 12° के गुणज को पार करे तब तिथि बदलती है।"
                ),
                HelpEntry(
                    icon = "⭐",
                    parameter = "नक्षत्र — चंद्र मंडल",
                    meaning = "आकाश को 27 नक्षत्रों में बाँटा गया है, प्रत्येक 13°20' का। किसी भी क्षण चंद्रमा जिस नक्षत्र में होता है, उसी से उस समय की गुणवत्ता तय होती है। जन्म नक्षत्र (जन्म नक्षत्र) मुहूर्त चयन और विवाह मिलान में केंद्रीय भूमिका निभाता है।",
                    calculation = "नक्षत्र = ⌊चंद्र साइडेरियल देशांतर ÷ 13.333⌋ + 1। प्रत्येक 13°20' की सीमा पार होने पर नक्षत्र बदलता है।"
                ),
                HelpEntry(
                    icon = "☯️",
                    parameter = "योग — संयुक्त प्रभाव",
                    meaning = "27 योग सूर्य और चंद्र के देशांतरों के योग से बनते हैं। ये शुभ (सिद्ध, शुभ, अमृत) से लेकर अशुभ (व्यतीपात, वैधृति) तक होते हैं। योग उस दिन की समग्र आध्यात्मिक ऊर्जा को दर्शाता है।",
                    calculation = "योग = ⌊((सूर्य देशांतर + चंद्र देशांतर) mod 360) ÷ 13.333⌋ + 1।"
                ),
                HelpEntry(
                    icon = "🔀",
                    parameter = "करण — अर्ध तिथि",
                    meaning = "करण तिथि का आधा भाग (6°) है। 11 करण होते हैं — 4 स्थिर (शकुनि, चतुष्पाद, नाग, किंस्तुघ्न) और 7 चर (बव, बालव, कौलव, तैतिल, गर, वणिज, विष्टि)। वर्तमान करण और अगला करण (\"→\") सूक्ष्म मुहूर्त चयन में उपयोगी होते हैं।",
                    calculation = "D = (चंद्र देशांतर − सूर्य देशांतर) mod 360। करण सूचकांक = ⌊D ÷ 6⌋, फिर 11-करण चक्र में मैप।"
                ),
                HelpEntry(
                    icon = "📅",
                    parameter = "वार — सप्ताह का दिन",
                    meaning = "प्रत्येक वार किसी ग्रह के अधीन है: रवि (रविवार), सोम (सोमवार), मंगल (मंगलवार), बुध (बुधवार), गुरु (गुरुवार), शुक्र (शुक्रवार), शनि (शनिवार)। शासक ग्रह उस दिन की ऊर्जा को प्रभावित करता है।",
                    calculation = "स्थानीय टाइमज़ोन रूपांतरण के बाद नागरिक तिथि का weekday लिया जाता है।"
                )
            )
        ),
        HelpSection(
            icon = "🌅",
            title = "सूर्य, चंद्र और काल-गणना",
            intro = "सभी समय-आधारित गणनाएँ आपके निर्देशांक और तिथि के अनुसार स्थानीय सूर्योदय और सूर्यास्त पर आधारित होती हैं। स्थान बदलने पर सभी काल स्वतः अपडेट हो जाते हैं।",
            entries = listOf(
                HelpEntry(
                    icon = "🌅",
                    parameter = "सूर्योदय और सूर्यास्त",
                    meaning = "आपके स्थान पर सूर्य के उदय और अस्त का स्थानीय समय। ये सभी पंचांग काल-खंडों — राहु काल, मुहूर्त और अशुभ काल — के आधार हैं।",
                    calculation = "Jean Meeus के सौर स्थिति समीकरणों से, वायुमंडलीय अपवर्तन (+0.5667°) और आपके अक्षांश-देशांतर के क्षितिज सुधार के साथ गणना।"
                ),
                HelpEntry(
                    icon = "🌕",
                    parameter = "चंद्रोदय और चंद्रास्त",
                    meaning = "स्थानीय स्थान पर चंद्रमा के उदय और अस्त का समय। रात्रि अनुष्ठान, तिथि परिवर्तन और व्रत समाप्ति के लिए उपयोगी। चंद्रमा ~13°/दिन की गति से चलता है, इसलिए चंद्रोदय प्रतिदिन ~50 मिनट देर से होता है।",
                    calculation = "दिनभर के चंद्र स्थिति नमूनों से क्षितिज-सीमा लाँघने का समय प्रक्षेप (interpolation) द्वारा निकाला जाता है।"
                ),
                HelpEntry(
                    icon = "⚠️",
                    parameter = "राहु काल",
                    meaning = "राहु (उत्तर चंद्र नोड) द्वारा शासित ~90 मिनट का दैनिक काल। परंपरागत रूप से सबसे अशुभ समय — नया काम शुरू करना, यात्रा, अनुबंध पर हस्ताक्षर, या महत्वपूर्ण निर्णय इस समय वर्जित हैं।",
                    calculation = "दिन (सूर्योदय से सूर्यास्त) को 8 बराबर खंडों में बाँटा जाता है। वार अनुसार खंड: रवि=8, सोम=2, मंगल=7, बुध=5, गुरु=6, शुक्र=4, शनि=3।"
                ),
                HelpEntry(
                    icon = "💀",
                    parameter = "यमगंड",
                    meaning = "यम देवता (मृत्यु के देवता) द्वारा शासित काल। राहु काल के बाद दूसरा सबसे अशुभ समय। इस अवधि में शुभ कार्य, उत्सव और नई शुरुआत से बचें।",
                    calculation = "राहु काल के समान 8-खंड विभाजन। वार अनुसार: रवि=5, सोम=4, मंगल=3, बुध=2, गुरु=1, शुक्र=8, शनि=7।"
                ),
                HelpEntry(
                    icon = "🪐",
                    parameter = "गुलिक काल",
                    meaning = "शनि के पुत्र गुलिक (मंद) का काल। राहु काल से कम गंभीर, परंतु शुभ कार्यों के लिए वर्जित। इस काल में बाधाएँ और विलंब अधिक होते हैं।",
                    calculation = "वार अनुसार 8वाँ खंड चयन: रवि=6, सोम=5, मंगल=4, बुध=3, गुरु=2, शुक्र=1, शनि=8।"
                ),
                HelpEntry(
                    icon = "🙏",
                    parameter = "ब्रह्म मुहूर्त",
                    meaning = "सूर्योदय से लगभग 96 मिनट पहले का \"ब्रह्म का समय\"। ध्यान, योग, प्राणायाम और शास्त्र-अध्ययन के लिए दिन का सर्वश्रेष्ठ काल। आयुर्वेद और योग-शास्त्र में इस समय जागने की सिफारिश की गई है।",
                    calculation = "आपके स्थान पर उस दिन के सूर्योदय समय से पूर्व निश्चित अवधि के रूप में गणना।"
                ),
                HelpEntry(
                    icon = "⚡",
                    parameter = "अभिजित मुहूर्त",
                    meaning = "स्थानीय सौर मध्याह्न के इर्द-गिर्द ~48 मिनट का अत्यंत शुभ मुहूर्त। नया व्यवसाय, महत्वपूर्ण निर्णय, अनुबंध हस्ताक्षर, और सफलता की इच्छा वाले किसी भी कार्य के लिए उत्तम। 15 दिवसीय मुहूर्तों में आठवाँ मुहूर्त है।",
                    calculation = "दिन को 15 बराबर मुहूर्तों में बाँटकर मध्याह्न-केंद्रित खंड लिया जाता है। अवधि = दिन की लंबाई ÷ 15।"
                ),
                HelpEntry(
                    icon = "🏆",
                    parameter = "विजय मुहूर्त",
                    meaning = "अपराह्न का \"विजय काल\"। प्रतिस्पर्धी गतिविधियों, कठिन कार्यों और बाधाओं पर विजय पाने के लिए शुभ। शास्त्रों में युद्ध, वाद-विवाद और महत्वाकांक्षी आरंभ के लिए अनुशंसित।",
                    calculation = "15-भाग दिवसीय मुहूर्त विभाजन का 14वाँ खंड।"
                ),
                HelpEntry(
                    icon = "🌆",
                    parameter = "गोधूलि मुहूर्त",
                    meaning = "सूर्यास्त के समीप \"गाय-धूल का समय\" — जब चरवाहे लौटती गायों की सुनहरी धूल उड़ाते हैं। गृह-प्रवेश, विवाह संस्कार, और पवित्र अनुष्ठानों के लिए विशेष शुभ। छोटी अवधि का पर अत्यंत मंगलकारी मुहूर्त।",
                    calculation = "ठीक सूर्यास्त समय को केंद्र मानकर सममित लघु-खंड।"
                ),
                HelpEntry(
                    icon = "🕉️",
                    parameter = "दिवसीय 15 मुहूर्त",
                    meaning = "सूर्योदय से सूर्यास्त तक का दिन 15 बराबर खंडों में बँटा होता है, प्रत्येक किसी देवता के नाम पर: रुद्र, अहि, मित्र, पितृ, वसु, वाराह, विश्वदेव, विधि, शतमुखी, पुरुहूत, वह्नि, नक्तंचर, वरुण, अर्यमा, भग। विभिन्न गतिविधियों के पारंपरिक नियोजन में उपयोगी।",
                    calculation = "मुहूर्त-अवधि = (सूर्यास्त − सूर्योदय) ÷ 15। खंड n = [सूर्योदय + (n−1)×अवधि, सूर्योदय + n×अवधि]।"
                )
            )
        ),
        HelpSection(
            icon = "📐",
            title = "सूत्र संदर्भ",
            intro = "मुख्य इनपुट: अक्षांश (φ), देशांतर (λ), स्थानीय तिथि और समय-क्षेत्र। प्रमुख चिह्न: Ls = सूर्य साइडेरियल देशांतर, Lm = चंद्र साइडेरियल देशांतर, JD = जूलियन दिवस।",
            entries = listOf(
                HelpEntry(
                    icon = "🌙",
                    parameter = "तिथि सूत्र",
                    meaning = "कोणीय चंद्र-दिवस सूचकांक। परास: 1–30।",
                    calculation = "D = (Lm − Ls) mod 360; तिथि = ⌊D ÷ 12⌋ + 1। एक तिथि = 12°।"
                ),
                HelpEntry(
                    icon = "⭐",
                    parameter = "नक्षत्र सूत्र",
                    meaning = "चंद्र नक्षत्र सूचकांक। परास: 1–27।",
                    calculation = "नक्षत्र = ⌊(Lm mod 360) ÷ 13.333⌋ + 1। प्रत्येक खंड = 13°20'।"
                ),
                HelpEntry(
                    icon = "☯️",
                    parameter = "योग सूत्र",
                    meaning = "सूर्य-चंद्र संयुक्त सूचकांक। परास: 1–27।",
                    calculation = "योग = ⌊((Ls + Lm) mod 360) ÷ 13.333⌋ + 1।"
                ),
                HelpEntry(
                    icon = "🔀",
                    parameter = "करण सूत्र",
                    meaning = "अर्ध-तिथि सूचकांक। एक करण = 6°।",
                    calculation = "D = (Lm − Ls) mod 360; करण = ⌊D ÷ 6⌋, फिर 11-करण चक्र में मैप।"
                ),
                HelpEntry(
                    icon = "⚠️",
                    parameter = "अशुभ काल सूत्र",
                    meaning = "राहु काल, यमगंड, गुलिक काल — खंड चयन।",
                    calculation = "खंड-लंबाई = (सूर्यास्त − सूर्योदय) ÷ 8। वार-मैप से 1–8 में से उचित खंड चुना जाता है।"
                ),
                HelpEntry(
                    icon = "⏱️",
                    parameter = "मुहूर्त विभाजन सूत्र",
                    meaning = "दिन को 15 बराबर मुहूर्त खंडों में बाँटना।",
                    calculation = "L = (सूर्यास्त − सूर्योदय) ÷ 15। खंड n = [सूर्योदय + (n−1)×L, सूर्योदय + n×L]।"
                )
            )
        ),
        HelpSection(
            icon = "🔭",
            title = "खगोलीय मान और त्योहार",
            intro = "ऐप के आंतरिक उपयोग में काम आने वाले कच्चे खगोलीय डेटा और त्योहार-पहचान तर्क।",
            entries = listOf(
                HelpEntry(
                    icon = "☀️",
                    parameter = "सूर्य और चंद्र देशांतर",
                    meaning = "साइडेरियल (निरायण) देशांतर — उष्णकटिबंधीय राशिचक्र की बजाय स्थिर तारों के सापेक्ष। ऐप लाहिरी-शैली के अयनांश (~23.85° वर्तमान युग) से उष्णकटिबंधीय को साइडेरियल में बदलता है। ये तिथि, नक्षत्र और योग के मूल इनपुट हैं।",
                    calculation = "Jean Meeus के ग्रहीय स्थिति एल्गोरिदम से observation time पर JD के लिए गणना।"
                ),
                HelpEntry(
                    icon = "📆",
                    parameter = "जूलियन दिवस (JD)",
                    meaning = "ईसा पूर्व 4713 जनवरी 1 की मध्याह्न से निरंतर दिन-गणना। खगोलशास्त्री कैलेंडर अस्पष्टता से बचने के लिए JD का उपयोग करते हैं। दशमलव भाग दिन के भीतर का समय दर्शाता है।",
                    calculation = "JD = 367×Y − ⌊7×(Y+⌊(M+9)÷12⌋)÷4⌋ + ⌊275×M÷9⌋ + D + 1721013.5 + UT÷24।"
                ),
                HelpEntry(
                    icon = "🎉",
                    parameter = "त्योहार पहचान",
                    meaning = "त्योहारों की पहचान तिथि, चंद्र मास, पक्ष और विशेष शर्तों (जैसे नक्षत्र या वार संयोग) को अंतर्निर्मित नियम तालिका से मिलाकर होती है। कोई बाहरी डेटा स्रोत नहीं।",
                    calculation = "नियम-आधारित मिलान: उदाहरण — दीवाली = कार्तिक कृष्ण अमावस्या; होली = फाल्गुन पूर्णिमा।"
                )
            )
        )
    )

    // ── Sanskrit ───────────────────────────────────────────────────────────────

    private val SA_SECTIONS = listOf(
        HelpSection(
            icon = "📖",
            title = "अनुप्रयोगस्य परिचयः",
            intro = "अयं वैदिकपञ्चाङ्ग-अनुप्रयोगः Jean Meeus-महोदयस्य खगोल-सूत्राणाम् आधारेण चयनिततिथेः स्थानस्य च अनुसारं सर्वाणि मानानि स्वयमेव गणयति। अन्तर्जालं बाह्य-API च न आवश्यकम्।",
            entries = emptyList()
        ),
        HelpSection(
            icon = "🕉️",
            title = "पञ्चाङ्गस्य पञ्च प्रधानाङ्गानि",
            intro = "\"पञ्चाङ्ग\" शब्दस्य अर्थः \"पञ्च अङ्गानि\" इति। एतानि पञ्च तत्त्वानि मिलित्वा वैदिककाले किमपि क्षणस्य गुणविशेषं सम्पूर्णतया वर्णयन्ति।",
            entries = listOf(
                HelpEntry(
                    icon = "🌙",
                    parameter = "तिथिः — चान्द्रदिनम्",
                    meaning = "तिथिः सा चान्द्र-इकाई यस्यां सूर्य-चन्द्रयोः कोणान्तरं 12° वर्धते। एकस्मिन् चान्द्रमासे 30 तिथयः सन्ति — 15 शुक्लपक्षे, 15 कृष्णपक्षे। व्रत-उत्सव-शुभकार्याणां निर्धारणे तिथिः प्रधाना भवति।",
                    calculation = "D = (चन्द्रदेशान्तरम् − सूर्यदेशान्तरम्) mod 360। तिथिः = ⌊D ÷ 12⌋ + 1। यदा D 12°-गुणजं लङ्घति तदा तिथिः परिवर्तते।"
                ),
                HelpEntry(
                    icon = "⭐",
                    parameter = "नक्षत्रम् — चन्द्रमण्डलम्",
                    meaning = "आकाशः 27 नक्षत्रेषु विभक्तः, प्रत्येकं 13°20' विस्तारम्। यस्मिन् नक्षत्रे चन्द्रः तिष्ठति तस्य गुणः तस्मिन् काले प्रबलः। जन्मनक्षत्रं मुहूर्त-निर्णये विवाह-मिलने च केन्द्रीयम्।",
                    calculation = "नक्षत्रम् = ⌊चन्द्रसाइडेरियल-देशान्तरम् ÷ 13.333⌋ + 1। प्रत्येकं 13°20'-सीमां लङ्घित्वा नक्षत्रं परिवर्तते।"
                ),
                HelpEntry(
                    icon = "☯️",
                    parameter = "योगः — संयुक्तप्रभावः",
                    meaning = "सूर्य-चन्द्रदेशान्तरयोः योगात् 27 योगाः निर्मिताः। एते शुभाः (सिद्धः, शुभः, अमृतः) तः अशुभाः (व्यतीपातः, वैधृतिः) पर्यन्तं सन्ति। योगः दिनस्य समग्रां आध्यात्मिक-ऊर्जां दर्शयति।",
                    calculation = "योगः = ⌊((Ls + Lm) mod 360) ÷ 13.333⌋ + 1।"
                ),
                HelpEntry(
                    icon = "🔀",
                    parameter = "करणम् — अर्धतिथिः",
                    meaning = "करणं तिथेः अर्धभागः (6°)। 11 करणानि सन्ति — 4 स्थिराणि (शकुनि, चतुष्पाद, नाग, किंस्तुघ्न) तथा 7 चराणि (बव, बालव, कौलव, तैतिल, गर, वणिज, विष्टि)। वर्तमानकरणं तदनन्तरकरणं च (\"→\") सूक्ष्ममुहूर्त-निर्णये प्रयुज्यते।",
                    calculation = "D = (Lm − Ls) mod 360। करण-सूचकाङ्कः = ⌊D ÷ 6⌋, ततः 11-करण-चक्रे नियोजनम्।"
                ),
                HelpEntry(
                    icon = "📅",
                    parameter = "वासरः — साप्ताहिकदिनम्",
                    meaning = "प्रत्येको वासरः कस्यचित् ग्रहस्य अधीनः — रविः (आदित्यवासरः), सोमः (सोमवासरः), कुजः (मङ्गलवासरः), बुधः (बुधवासरः), गुरुः (गुरुवासरः), शुक्रः (शुक्रवासरः), शनिः (शनिवासरः)। स्वामिग्रहः तद्दिनस्य ऊर्जां प्रभावयति।",
                    calculation = "स्थानिक-समयसरण्याः रूपान्तरणानन्तरं नागरिकतिथेः weekday इति गृह्यते।"
                )
            )
        ),
        HelpSection(
            icon = "🌅",
            title = "सूर्य-चन्द्र-कालगणना",
            intro = "सर्वे कालाः भवतः निर्देशाङ्क-स्थानिकतिथ्योः अनुसारं स्थानिक-सूर्योदय-सूर्यास्तयोः आधारेण निर्धार्यन्ते। स्थानपरिवर्तने सर्वे कालाः स्वयमेव नवीक्रियन्ते।",
            entries = listOf(
                HelpEntry(
                    icon = "🌅",
                    parameter = "सूर्योदयः / सूर्यास्तः",
                    meaning = "भवतः स्थाने सूर्यस्य उदय-अस्त-कालः। समस्त-पञ्चाङ्ग-कालखण्डानाम् — राहुकाल-मुहूर्त-अशुभकालानां — एते मूलाधाराः सन्ति।",
                    calculation = "Jean Meeus-सौरस्थितिसमीकरणैः सह वायुमण्डलीय-अपवर्तन (+0.5667°) तथा अक्षांश-देशान्तर-क्षितिजसंशोधनेन गणना।"
                ),
                HelpEntry(
                    icon = "🌕",
                    parameter = "चन्द्रोदयः / चन्द्रास्तः",
                    meaning = "स्थानिके चन्द्रस्य उदय-अस्त-कालः। निशा-अनुष्ठान-तिथिपरिवर्तन-व्रतसमाप्त्यर्थम् उपयोगी। चन्द्रः ~13°/दिने गच्छति, अतः चन्द्रोदयः प्रतिदिनं ~50 निमेषैः विलम्बते।",
                    calculation = "दिने चन्द्रस्थिति-नमूनानां प्रक्षेपणेन (interpolation) क्षितिजलङ्घन-कालः निर्णीयते।"
                ),
                HelpEntry(
                    icon = "⚠️",
                    parameter = "राहुकालः",
                    meaning = "राहोः (उत्तर-चन्द्रनोड) ~90-निमेष-दैनिककालः। परम्परया अत्यन्तम् अशुभः — नवकार्यारम्भः, यात्रा, सन्धिः, महत्त्वपूर्णनिर्णयः च वर्ज्यः। वासरानुसारं भिन्नः खण्डः।",
                    calculation = "दिनं 8 समखण्डेषु विभज्यते। वासर-मानचित्रम्: रवि=8, सोम=2, कुज=7, बुध=5, गुरु=6, शुक्र=4, शनि=3।"
                ),
                HelpEntry(
                    icon = "💀",
                    parameter = "यमगण्डः",
                    meaning = "यमस्य (मृत्युदेवस्य) कालः। राहुकालात् परं द्वितीयः अशुभकालः। अस्मिन् काले शुभकार्याणि क्रियाकलापाः नवारम्भाः च वर्ज्याः।",
                    calculation = "राहुकालस्य समानो 8-खण्ड-विभागः। वासर-मानचित्रम्: रवि=5, सोम=4, कुज=3, बुध=2, गुरु=1, शुक्र=8, शनि=7।"
                ),
                HelpEntry(
                    icon = "🪐",
                    parameter = "गुलिककालः",
                    meaning = "शनिपुत्रस्य गुलिकस्य (मन्दस्य) कालः। राहुकालात् न्यूनतीव्रः, परन्तु शुभकार्येषु वर्ज्यः। अस्मिन् काले विघ्नाः विलम्बाः च अधिकाः।",
                    calculation = "वासर-मानचित्रम्: रवि=6, सोम=5, कुज=4, बुध=3, गुरु=2, शुक्र=1, शनि=8।"
                ),
                HelpEntry(
                    icon = "🙏",
                    parameter = "ब्रह्ममुहूर्तः",
                    meaning = "सूर्योदयात् ~96-निमेषपूर्वस्य \"ब्रह्मकालः\"। ध्यान-योग-प्राणायाम-शास्त्राध्ययनाय दिनस्य श्रेष्ठकालः। आयुर्वेद-योगशास्त्रे अस्मिन् काले उत्थानं प्रशस्तम्।",
                    calculation = "भवतः स्थाने तद्दिनस्य सूर्योदयकालात् पूर्वं निश्चितावधिः।"
                ),
                HelpEntry(
                    icon = "⚡",
                    parameter = "अभिजित्मुहूर्तः",
                    meaning = "स्थानीय-सौरमध्याह्नस्य समीपे ~48-निमेषस्य अत्यन्तं शुभः मुहूर्तः। नवव्यवसाय-महत्त्वपूर्णनिर्णय-सन्धिहस्ताक्षरणाय उत्कृष्टः। 15 दिवामुहूर्तेषु अष्टमः।",
                    calculation = "दिनं 15 सममुहूर्तेषु विभज्य मध्याह्न-केन्द्रितः खण्डः। अवधिः = दिनदीर्घता ÷ 15।"
                ),
                HelpEntry(
                    icon = "🏆",
                    parameter = "विजयमुहूर्तः",
                    meaning = "अपराह्णस्य \"विजयकालः\"। स्पर्धात्मककार्येषु, कठिनकार्येषु, विघ्नजयाय शुभः। शास्त्रेषु युद्ध-वाद-महत्त्वाकांक्षिणः कार्यारम्भाय अनुशंसितः।",
                    calculation = "15-भाग-दिवसविभागस्य 14-तमः खण्डः।"
                ),
                HelpEntry(
                    icon = "🌆",
                    parameter = "गोधूलिमुहूर्तः",
                    meaning = "सूर्यास्तसमीपे \"गोधूलिकालः\" — यस्मिन् गोपालाः गृहागतानां गवाम् धूलिः विकिरति। गृहप्रवेश-विवाह-पवित्रानुष्ठानेषु विशेषतः शुभः। लघुः परन्तु अत्युत्कृष्टः।",
                    calculation = "ठीक सूर्यास्तकालं केन्द्रं कृत्वा सममितः लघुखण्डः।"
                ),
                HelpEntry(
                    icon = "🕉️",
                    parameter = "दिवामुहूर्ताः (15)",
                    meaning = "सूर्योदयात् सूर्यास्तपर्यन्तं दिनं 15 समखण्डेषु विभक्तम्, प्रत्येकं देवतानाम्ना: रुद्रः, अहिः, मित्रः, पितृः, वसुः, वाराहः, विश्वदेवः, विधिः, शतमुखी, पुरुहूतः, वह्निः, नक्तंचरः, वरुणः, अर्यमा, भगः। विविध-क्रियाणां पारम्परिक-नियोजने उपयोगी।",
                    calculation = "मुहूर्तावधिः = (सूर्यास्तः − सूर्योदयः) ÷ 15। खण्डः n = [सूर्योदयः + (n−1)×L, सूर्योदयः + n×L]।"
                )
            )
        ),
        HelpSection(
            icon = "📐",
            title = "सूत्र-सन्दर्भः",
            intro = "मुख्य-आदानानि: अक्षांशः (φ), देशान्तरम् (λ), स्थानिकतिथिः, समयसरणिः। मुख्यचिह्नानि: Ls = सूर्य-साइडेरियल-देशान्तरम्, Lm = चन्द्र-साइडेरियल-देशान्तरम्, JD = जूलियन-दिवसः।",
            entries = listOf(
                HelpEntry(
                    icon = "🌙",
                    parameter = "तिथेः सूत्रम्",
                    meaning = "कोणाधारितः चान्द्रदिन-सूचकाङ्कः। परासः: 1–30।",
                    calculation = "D = (Lm − Ls) mod 360; तिथिः = ⌊D ÷ 12⌋ + 1। एक तिथिः = 12°।"
                ),
                HelpEntry(
                    icon = "⭐",
                    parameter = "नक्षत्रस्य सूत्रम्",
                    meaning = "चन्द्रनक्षत्र-सूचकाङ्कः। परासः: 1–27।",
                    calculation = "नक्षत्रम् = ⌊(Lm mod 360) ÷ 13.333⌋ + 1। प्रत्येकः खण्डः = 13°20'।"
                ),
                HelpEntry(
                    icon = "☯️",
                    parameter = "योगस्य सूत्रम्",
                    meaning = "सूर्य-चन्द्र-योग-सूचकाङ्कः। परासः: 1–27।",
                    calculation = "योगः = ⌊((Ls + Lm) mod 360) ÷ 13.333⌋ + 1।"
                ),
                HelpEntry(
                    icon = "🔀",
                    parameter = "करणस्य सूत्रम्",
                    meaning = "अर्धतिथि-सूचकाङ्कः। एकं करणम् = 6°।",
                    calculation = "D = (Lm − Ls) mod 360; करण-सूचकाङ्कः = ⌊D ÷ 6⌋, ततः 11-करण-चक्रे नियोजनम्।"
                ),
                HelpEntry(
                    icon = "⚠️",
                    parameter = "अशुभकाल-सूत्रम्",
                    meaning = "राहुकालः, यमगण्डः, गुलिककालः — खण्डचयनम्।",
                    calculation = "खण्डदीर्घता = (सूर्यास्तः − सूर्योदयः) ÷ 8। वासरमानचित्रेण 1–8 मध्ये उचितः खण्डः चयन्यते।"
                ),
                HelpEntry(
                    icon = "⏱️",
                    parameter = "मुहूर्त-विभाजनम्",
                    meaning = "दिवसस्य 15 सममुहूर्त-खण्डेषु विभाजनम्।",
                    calculation = "L = (सूर्यास्तः − सूर्योदयः) ÷ 15। खण्डः n = [सूर्योदयः + (n−1)×L, सूर्योदयः + n×L]।"
                )
            )
        ),
        HelpSection(
            icon = "🔭",
            title = "खगोलीयमानानि तथा उत्सवाः",
            intro = "अन्तःप्रयोगे उपयुक्तानि कच्च-खगोलीय-डेटा तथा उत्सव-परिचय-नियमाः।",
            entries = listOf(
                HelpEntry(
                    icon = "☀️",
                    parameter = "सूर्य-चन्द्र-देशान्तरम्",
                    meaning = "साइडेरियल (निरयन) देशान्तरः — उष्णकटिबन्धीय-राशिचक्रस्य स्थाने स्थिर-तारकाभिः सापेक्षः। अनुप्रयोगः लाहिरी-अयनांशेन (~23.85° वर्तमानयुगे) उष्णकटिबन्धीयं साइडेरियल-देशान्तरं करोति। एतानि तिथि-नक्षत्र-योगस्य मूल-आदानानि।",
                    calculation = "Jean Meeus-ग्रह-स्थितिसूत्रैः observation time पर JD-आधारितं गणनम्।"
                ),
                HelpEntry(
                    icon = "📆",
                    parameter = "जूलियन-दिवसः (JD)",
                    meaning = "ईसा-पूर्व 4713 वर्षस्य जनवरी 1 मध्याह्नात् आरभ्य निरन्तर-दिवस-गणना। खगोलविदाः calendar-अस्पष्टतां टाळयितुं JD प्रयुञ्जते। दशांशः दिनान्तर्गत-समयं सूचयति।",
                    calculation = "JD = 367×Y − ⌊7×(Y+⌊(M+9)÷12⌋)÷4⌋ + ⌊275×M÷9⌋ + D + 1721013.5 + UT÷24।"
                ),
                HelpEntry(
                    icon = "🎉",
                    parameter = "उत्सव-परिचयः",
                    meaning = "उत्सवाः तिथि, चन्द्रमास, पक्ष तथा विशेष-शर्तैः (यथा नक्षत्र/वार-संयोगः) नियम-सूची-योजनया निश्चीयन्ते। बाह्य-डेटा न प्रयुज्यते।",
                    calculation = "नियम-आधारित-मिलानम्: उदा — दीपावली = कार्तिक-कृष्ण-अमावस्या; होली = फाल्गुन-पूर्णिमा।"
                )
            )
        )
    )
}
