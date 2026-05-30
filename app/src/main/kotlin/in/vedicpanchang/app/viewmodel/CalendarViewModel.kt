package `in`.vedicpanchang.app.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.vedicpanchang.app.data.datasource.AppPreferences
import `in`.vedicpanchang.app.data.datasource.db.NoteDao
import `in`.vedicpanchang.app.data.datasource.db.toDomain
import `in`.vedicpanchang.app.data.datasource.db.toEntity
import `in`.vedicpanchang.app.data.model.CustomCalendarNote
import `in`.vedicpanchang.app.data.model.RepeatType
import `in`.vedicpanchang.app.service.NotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

// ── Result types ──────────────────────────────────────────────────────────────

enum class AddNoteResult { SAVED, SAVED_WITHOUT_NOTIFICATION, ERROR }

// ── UI events (one-shot) ──────────────────────────────────────────────────────

sealed interface CalendarEvent {
    data class NoteAdded(val result: AddNoteResult) : CalendarEvent
    data class NoteDeleted(val success: Boolean) : CalendarEvent
    data class Error(val message: String) : CalendarEvent
}

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface NotesUiState {
    data object Loading : NotesUiState
    data class Success(
        val notes: List<CustomCalendarNote>,
        val notesByDay: Map<LocalDate, List<CustomCalendarNote>>
    ) : NotesUiState
    data class Error(val message: String) : NotesUiState
}

/**
 * Manages calendar notes (Room CRUD) and selected-date state.
 * Equivalent of custom_calendar_notes_provider.dart.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val notificationService: NotificationService,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    /** All notes as a live Room Flow, mapped to domain models. */
    val notesState: StateFlow<NotesUiState> = noteDao.observeAll()
        .map { entities ->
            val notes = entities.map { it.toDomain() }
            val byDay = buildMap<LocalDate, MutableList<CustomCalendarNote>> {
                notes.forEach { note ->
                    getOrPut(note.date) { mutableListOf() }.add(note)
                    if (note.repeatType != RepeatType.NONE) {
                        for (i in 1..note.repeatType.occurrenceCount) {
                            val projected = when (note.repeatType) {
                                RepeatType.DAILY   -> note.date.plus(i, DateTimeUnit.DAY)
                                RepeatType.WEEKLY  -> note.date.plus(i * 7, DateTimeUnit.DAY)
                                RepeatType.MONTHLY -> note.date.plus(i, DateTimeUnit.MONTH)
                                RepeatType.YEARLY  -> note.date.plus(i, DateTimeUnit.YEAR)
                                RepeatType.NONE    -> note.date
                            }
                            getOrPut(projected) { mutableListOf() }.add(note)
                        }
                    }
                }
            }
            NotesUiState.Success(notes, byDay) as NotesUiState
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState.Loading)

    /** Notes for the currently selected date as a live Room Flow. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val notesForSelectedDate: StateFlow<List<CustomCalendarNote>> =
        _selectedDate.flatMapLatest { date ->
            val epochDay = date.toJavaLocalDate().toEpochDay()
            noteDao.observeForDate(epochDay).map { entities -> entities.map { it.toDomain() } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = MutableSharedFlow<CalendarEvent>()
    val events: SharedFlow<CalendarEvent> = _events.asSharedFlow()

    // ── Date selection ────────────────────────────────────────────────────────

    fun selectDate(date: LocalDate) {
        _selectedDate.update { date }
    }

    // ── Note CRUD ─────────────────────────────────────────────────────────────

    fun addNote(
        date: LocalDate,
        title: String,
        description: String = "",
        reminderAt: Instant? = null,
        repeatType: RepeatType = RepeatType.NONE
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = Clock.System.now()
                val resolvedReminder = resolveReminderTime(reminderAt, now)
                var persistedReminder = resolvedReminder
                var allowNotification = true

                if (resolvedReminder != null) {
                    if (!notificationService.canScheduleExactAlarms()) {
                        allowNotification = false
                        persistedReminder = null
                    }
                }

                val note = CustomCalendarNote(
                    id = 0,
                    date = date,
                    title = title.trim(),
                    description = description.trim(),
                    reminderAt = persistedReminder,
                    repeatType = repeatType,
                    createdAt = now
                )

                val insertedId = noteDao.insert(note.toEntity()).toInt()
                val noteWithId = note.copy(id = insertedId)

                if (persistedReminder != null && allowNotification) {
                    val body = note.description.ifEmpty { "Reminder" }
                    val scheduled = notificationService.schedule(
                        id = noteWithId.notificationId,
                        title = "🗓️ ${noteWithId.title}",
                        body = body,
                        triggerAtMs = persistedReminder.toEpochMilliseconds()
                    )
                    if (!scheduled) {
                        allowNotification = false
                        noteDao.update(noteWithId.copy(reminderAt = null).toEntity())
                    } else if (repeatType != RepeatType.NONE) {
                        scheduleRepeatOccurrences(noteWithId, persistedReminder, repeatType)
                    }
                }

                _events.emit(
                    CalendarEvent.NoteAdded(
                        if (allowNotification) AddNoteResult.SAVED
                        else AddNoteResult.SAVED_WITHOUT_NOTIFICATION
                    )
                )
            } catch (e: Exception) {
                _events.emit(CalendarEvent.Error(e.message ?: "Failed to save note"))
                _events.emit(CalendarEvent.NoteAdded(AddNoteResult.ERROR))
            }
        }
    }

    fun deleteNote(note: CustomCalendarNote) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (note.reminderAt != null) {
                    notificationService.cancel(note.notificationId)
                    if (note.repeatType != RepeatType.NONE) {
                        for (i in 1..note.repeatType.occurrenceCount) {
                            notificationService.cancel(note.notificationId + i)
                        }
                    }
                }
                noteDao.deleteById(note.id)
                _events.emit(CalendarEvent.NoteDeleted(true))
            } catch (e: Exception) {
                _events.emit(CalendarEvent.Error(e.message ?: "Failed to delete note"))
                _events.emit(CalendarEvent.NoteDeleted(false))
            }
        }
    }

    private fun scheduleRepeatOccurrences(
        note: CustomCalendarNote,
        baseInstant: Instant,
        repeatType: RepeatType
    ) {
        if (repeatType == RepeatType.NONE) return
        val tz = TimeZone.currentSystemDefault()
        val baseLocal = baseInstant.toLocalDateTime(tz)
        val time = baseLocal.time
        for (i in 1..repeatType.occurrenceCount) {
            val nextDate = when (repeatType) {
                RepeatType.DAILY   -> note.date.plus(i, DateTimeUnit.DAY)
                RepeatType.WEEKLY  -> note.date.plus(i * 7, DateTimeUnit.DAY)
                RepeatType.MONTHLY -> note.date.plus(i, DateTimeUnit.MONTH)
                RepeatType.YEARLY  -> note.date.plus(i, DateTimeUnit.YEAR)
                RepeatType.NONE    -> return
            }
            val triggerMs = LocalDateTime(nextDate, time).toInstant(tz).toEpochMilliseconds()
            notificationService.schedule(
                id = note.notificationId + i,
                title = "🗓️ ${note.title}",
                body = note.description.ifEmpty { "Reminder" },
                triggerAtMs = triggerMs
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun resolveReminderTime(reminderAt: Instant?, now: Instant): Instant? {
        if (reminderAt == null) return null
        return if (reminderAt <= now) {
            // Past time — trigger soon instead of silently skipping
            Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + 10_000L)
        } else {
            reminderAt
        }
    }
}
