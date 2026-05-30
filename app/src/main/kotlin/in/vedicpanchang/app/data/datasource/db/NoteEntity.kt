package `in`.vedicpanchang.app.data.datasource.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateEpochDay: Long,          // LocalDate.toEpochDays()
    val title: String,
    val description: String,
    val reminderAtMs: Long?,         // Instant.toEpochMilliseconds(), null if no reminder
    val repeatType: String = "NONE", // RepeatType name
    val createdAtMs: Long            // Instant.toEpochMilliseconds()
)
