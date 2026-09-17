package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follow_ups",
    indices = [
        Index("noteId"),
        Index("status")
    ]
)
data class FollowUpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val subject: String,
    val context: String,
    val person: String? = null,
    val project: String? = null,
    val status: String = "OPEN", // OPEN, WAITING, DUE, OVERDUE, RESOLVED
    val dueDateMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)
