package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentNotes(limit: Int = 10): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteById(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteByIdSync(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND sourceType = :sourceType ORDER BY createdAt DESC")
    fun getNotesBySource(sourceType: String): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE isArchived = 0 
        AND (
            title LIKE '%' || :query || '%' 
            OR originalInput LIKE '%' || :query || '%' 
            OR summary LIKE '%' || :query || '%' 
            OR ocrText LIKE '%' || :query || '%' 
            OR rawTranscript LIKE '%' || :query || '%'
            OR webContent LIKE '%' || :query || '%'
            OR people LIKE '%' || :query || '%'
            OR projects LIKE '%' || :query || '%'
            OR topics LIKE '%' || :query || '%'
            OR tags LIKE '%' || :query || '%'
        )
        ORDER BY createdAt DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isArchived = 0")
    suspend fun getAllNotesSync(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}
