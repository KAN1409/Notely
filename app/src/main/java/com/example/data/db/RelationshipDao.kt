package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RelationshipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationships ORDER BY createdAt DESC")
    fun getAllRelationships(): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationships WHERE sourceNoteId = :noteId OR targetNoteId = :noteId")
    fun getRelationshipsForNote(noteId: Long): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationships WHERE sourceNoteId = :noteId OR targetNoteId = :noteId")
    suspend fun getRelationshipsForNoteSync(noteId: Long): List<RelationshipEntity>

    @Query("""
        SELECT COUNT(*) FROM relationships 
        WHERE ((sourceNoteId = :sourceId AND targetNoteId = :targetId) 
           OR (sourceNoteId = :targetId AND targetNoteId = :sourceId))
          AND relationshipType = :type
    """)
    suspend fun countRelationship(sourceId: Long, targetId: Long, type: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRelationship(relationship: RelationshipEntity): Long

    @Query("DELETE FROM relationships WHERE sourceNoteId = :noteId OR targetNoteId = :noteId")
    suspend fun deleteRelationshipsForNote(noteId: Long)
}
