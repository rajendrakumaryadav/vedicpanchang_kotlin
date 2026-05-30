package `in`.vedicpanchang.app.data.model

enum class RepeatType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY;

    val occurrenceCount: Int get() = when (this) {
        NONE    -> 0
        DAILY   -> 30
        WEEKLY  -> 12
        MONTHLY -> 12
        YEARLY  -> 5
    }
}
