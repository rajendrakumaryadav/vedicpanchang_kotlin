package `in`.vedicpanchang.app.data.datasource.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM calendar_notes ORDER BY dateEpochDay ASC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM calendar_notes WHERE dateEpochDay = :epochDay ORDER BY createdAtMs ASC")
    fun observeForDate(epochDay: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM calendar_notes WHERE id = :id")
    suspend fun getById(id: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM calendar_notes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
