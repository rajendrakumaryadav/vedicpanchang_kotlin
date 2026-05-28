package `in`.vedicpanchang.app.data.model

data class FestivalModel(
    val name: String,
    val description: String,
    val type: FestivalType,
    val significance: List<String> = emptyList(),
    val observanceNote: String? = null
)

enum class FestivalType {
    EKADASHI, PURNIMA, AMAVASYA,
    FESTIVAL,   // Major festivals: Diwali, Holi, etc.
    VRAT,       // Fasting days
    SANKRANTI,  // Sun transit events
    NAVRATRI,
    OTHER;

    val label: String get() = when (this) {
        EKADASHI -> "Ekadashi"
        PURNIMA -> "Purnima"
        AMAVASYA -> "Amavasya"
        FESTIVAL -> "Festival"
        VRAT -> "Vrat"
        SANKRANTI -> "Sankranti"
        NAVRATRI -> "Navratri"
        OTHER -> "Event"
    }
}
