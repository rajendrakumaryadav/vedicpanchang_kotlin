package `in`.vedicpanchang.app.data.datasource.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import `in`.vedicpanchang.app.data.model.CustomCalendarNote
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

import java.time.LocalDate as JLocalDate

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                NoteDatabase::class.java,
                "vedic_panchang_notes.db"
            ).build().also { INSTANCE = it }
        }
    }
}

// ── Mappers ──────────────────────────────────────────────────────────────────

fun NoteEntity.toDomain(): CustomCalendarNote = CustomCalendarNote(
    id = id,
    date = LocalDate.fromEpochDays(dateEpochDay.toInt()),
    title = title,
    description = description,
    reminderAt = reminderAtMs?.let { Instant.fromEpochMilliseconds(it) },
    createdAt = Instant.fromEpochMilliseconds(createdAtMs)
)

fun CustomCalendarNote.toEntity(): NoteEntity = NoteEntity(
    id = id,
    dateEpochDay = date.toJavaLocalDate().toEpochDay(),
    title = title,
    description = description,
    reminderAtMs = reminderAt?.toEpochMilliseconds(),
    createdAtMs = createdAt.toEpochMilliseconds()
)
