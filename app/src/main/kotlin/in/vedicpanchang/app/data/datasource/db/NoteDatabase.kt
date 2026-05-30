package `in`.vedicpanchang.app.data.datasource.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import `in`.vedicpanchang.app.data.model.CustomCalendarNote
import `in`.vedicpanchang.app.data.model.RepeatType
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

import java.time.LocalDate as JLocalDate

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE calendar_notes ADD COLUMN repeatType TEXT NOT NULL DEFAULT 'NONE'"
        )
    }
}

@Database(entities = [NoteEntity::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                NoteDatabase::class.java,
                "vedic_panchang_notes.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
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
    repeatType = runCatching { RepeatType.valueOf(repeatType) }.getOrDefault(RepeatType.NONE),
    createdAt = Instant.fromEpochMilliseconds(createdAtMs)
)

fun CustomCalendarNote.toEntity(): NoteEntity = NoteEntity(
    id = id,
    dateEpochDay = date.toJavaLocalDate().toEpochDay(),
    title = title,
    description = description,
    reminderAtMs = reminderAt?.toEpochMilliseconds(),
    repeatType = repeatType.name,
    createdAtMs = createdAt.toEpochMilliseconds()
)
