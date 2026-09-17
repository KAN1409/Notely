package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.FollowUpEntity
import com.example.data.model.NoteEntity
import com.example.data.model.RelationshipEntity
import com.example.data.model.ReminderEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val database: AppDatabase) {
    private val noteDao = database.noteDao()
    private val relationshipDao = database.relationshipDao()
    private val followUpDao = database.followUpDao()
    private val reminderDao = database.reminderDao()

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getRecentNotes(limit: Int = 10): Flow<List<NoteEntity>> = noteDao.getRecentNotes(limit)

    fun getNoteById(id: Long): Flow<NoteEntity?> = noteDao.getNoteById(id)

    suspend fun getNoteByIdSync(id: Long): NoteEntity? = noteDao.getNoteByIdSync(id)

    fun getNotesBySource(sourceType: String): Flow<List<NoteEntity>> = noteDao.getNotesBySource(sourceType)

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun getAllNotesSync(): List<NoteEntity> = noteDao.getAllNotesSync()

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun archiveNote(id: Long, isArchived: Boolean) = noteDao.setArchived(id, isArchived)

    suspend fun deleteNote(id: Long) {
        relationshipDao.deleteRelationshipsForNote(id)
        followUpDao.deleteFollowUpsForNote(id)
        reminderDao.deleteRemindersForNote(id)
        noteDao.deleteNote(id)
    }

    // Relationships
    fun getAllRelationships(): Flow<List<RelationshipEntity>> = relationshipDao.getAllRelationships()

    fun getRelationshipsForNote(noteId: Long): Flow<List<RelationshipEntity>> =
        relationshipDao.getRelationshipsForNote(noteId)

    suspend fun insertRelationship(relationship: RelationshipEntity): Long {
        if (relationship.sourceNoteId == relationship.targetNoteId) return -1
        val exists = relationshipDao.countRelationship(
            relationship.sourceNoteId,
            relationship.targetNoteId,
            relationship.relationshipType
        )
        return if (exists == 0) {
            relationshipDao.insertRelationship(relationship)
        } else {
            -1
        }
    }

    // Follow-ups
    fun getAllFollowUps(): Flow<List<FollowUpEntity>> = followUpDao.getAllFollowUps()

    fun getOpenFollowUps(): Flow<List<FollowUpEntity>> = followUpDao.getOpenFollowUps()

    fun getFollowUpsByStatus(status: String): Flow<List<FollowUpEntity>> =
        followUpDao.getFollowUpsByStatus(status)

    suspend fun insertFollowUp(followUp: FollowUpEntity): Long = followUpDao.insertFollowUp(followUp)

    suspend fun updateFollowUp(followUp: FollowUpEntity) = followUpDao.updateFollowUp(followUp)

    suspend fun updateFollowUpStatus(id: Long, status: String) {
        val resolvedAt = if (status == "RESOLVED") System.currentTimeMillis() else null
        followUpDao.updateStatus(id, status, resolvedAt)
    }

    suspend fun deleteFollowUp(id: Long) = followUpDao.deleteFollowUp(id)

    // Reminders
    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    fun getActiveReminders(): Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()

    suspend fun getActiveRemindersSync(): List<ReminderEntity> = reminderDao.getActiveRemindersSync()

    suspend fun insertReminder(reminder: ReminderEntity): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)

    suspend fun setReminderCompleted(id: Long, completed: Boolean) =
        reminderDao.setCompleted(id, completed)

    suspend fun deleteReminder(id: Long) = reminderDao.deleteReminder(id)
}
