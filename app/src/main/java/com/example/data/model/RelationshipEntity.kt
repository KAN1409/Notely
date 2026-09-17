package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "relationships",
    indices = [
        Index("sourceNoteId"),
        Index("targetNoteId")
    ]
)
data class RelationshipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceNoteId: Long,
    val targetNoteId: Long,
    val relationshipType: String, // SAME_PROJECT, SAME_PERSON, SAME_TOPIC, UPDATE, RESOLVES, CONTRADICTION, RELATED
    val explanation: String,
    val createdAt: Long = System.currentTimeMillis()
)
