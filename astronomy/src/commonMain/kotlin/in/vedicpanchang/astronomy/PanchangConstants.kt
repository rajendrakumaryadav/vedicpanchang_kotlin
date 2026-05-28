package `in`.vedicpanchang.astronomy

object PanchangConstants {

    val TITHI_NAMES = listOf(
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima",
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Amavasya"
    )

    val PAKSHA_NAMES = listOf("Shukla", "Krishna")

    val NAKSHATRA_NAMES = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashirsha",
        "Ardra", "Punarvasu", "Pushya", "Ashlesha", "Magha",
        "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati",
        "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
        "Uttara Ashadha", "Shravana", "Dhanishtha", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )

    val YOGA_NAMES = listOf(
        "Vishkamba", "Preeti", "Ayushman", "Saubhagya", "Shobhana",
        "Atiganda", "Sukarma", "Dhriti", "Shula", "Ganda",
        "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra",
        "Siddhi", "Vyatipata", "Variyan", "Parigha", "Shiva",
        "Siddha", "Sadhya", "Shubha", "Shukla", "Brahma",
        "Indra", "Vaidhriti"
    )

    // 7 movable (index 0-6) + 4 fixed (index 7-10)
    val KARANA_NAMES = listOf(
        "Bava", "Balava", "Kaulava", "Taitula", "Garaja",
        "Vanija", "Vishti",         // 7 movable (Chara)
        "Shakuni", "Chatushpada", "Naga", "Kimstughna" // 4 fixed (Sthira)
    )

    val VAAR_NAMES = listOf(
        "Ravivaar", "Somvaar", "Mangalvaar", "Budhvaar",
        "Guruvaar", "Shukravaar", "Shanivaar"
    )

    val YOGA_AUSPICIOUS = mapOf(
        "Vishkamba" to false, "Preeti" to true, "Ayushman" to true,
        "Saubhagya" to true, "Shobhana" to true, "Atiganda" to false,
        "Sukarma" to true, "Dhriti" to true, "Shula" to false,
        "Ganda" to false, "Vriddhi" to true, "Dhruva" to true,
        "Vyaghata" to false, "Harshana" to true, "Vajra" to false,
        "Siddhi" to true, "Vyatipata" to false, "Variyan" to false,
        "Parigha" to false, "Shiva" to true, "Siddha" to true,
        "Sadhya" to true, "Shubha" to true, "Shukla" to true,
        "Brahma" to true, "Indra" to true, "Vaidhriti" to false
    )

    // Index = 0 (Sunday) to 6 (Saturday). Value = 1-indexed 1/8th-day block.
    val RAHU_KAAL_BLOCKS = listOf(8, 2, 7, 5, 6, 3, 4)
    val YAMAGANDA_BLOCKS = listOf(5, 4, 3, 2, 1, 7, 6)
    val GULIKA_BLOCKS = listOf(7, 6, 5, 4, 3, 2, 1)
}
