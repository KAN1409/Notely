package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FollowUpEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {
    @Query("SELECT * FROM follow_ups ORDER BY createdAt DESC")
    fun getAllFollowUps(): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE status != 'RESOLVED' ORDER BY dueDateMillis ASC, createdAt DESC")
    fun getOpenFollowUps(): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE status = :status ORDER BY dueDateMillis ASC")
    fun getFollowUpsByStatus(status: String): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE noteId = :noteId")
    fun getFollowUpsForNote(noteId: Long): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE id = :id LIMIT 1")
    suspend fun getFollowUpById(id: Long): FollowUpEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUpEntity): Long

    @Update
    suspend fun updateFollowUp(followUp: FollowUpEntity)

    @Query("UPDATE follow_ups SET status = :status, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, resolvedAt: Long? = null)

    @Query("DELETE FROM follow_ups WHERE id = :id")
    suspend fun deleteFollowUp(id: Long)

    @Query("DELETE FROM follow_ups WHERE noteId = :noteId")
    suspend fun deleteFollowUpsForNote(noteId: Long)
}
