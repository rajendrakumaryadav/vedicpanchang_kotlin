package `in`.vedicpanchang.app.data.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class CustomCalendarNote(
    val id: Int,
    val date: LocalDate,
    val title: String,
    val description: String,
    val reminderAt: Instant?,
    val createdAt: Instant
) {
    val notificationId: Int get() = 200000 + (id % 700000)

    fun copyWith(
        id: Int = this.id,
        date: LocalDate = this.date,
        title: String = this.title,
        description: String = this.description,
        reminderAt: Instant? = this.reminderAt,
        clearReminder: Boolean = false,
        createdAt: Instant = this.createdAt
    ) = copy(
        id = id, date = date, title = title, description = description,
        reminderAt = if (clearReminder) null else reminderAt,
        createdAt = createdAt
    )
}
