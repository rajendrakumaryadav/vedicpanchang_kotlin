package `in`.vedicpanchang.app.data.model

/** One help entry for a specific parameter. */
data class HelpEntry(
    val icon: String,
    val parameter: String,
    val meaning: String,
    val calculation: String
)

/** Group of help entries under one section. */
data class HelpSection(
    val icon: String,
    val title: String,
    val intro: String,
    val entries: List<HelpEntry>
)
