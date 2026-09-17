package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index("createdAt"),
        Index("sourceType"),
        Index("noteType"),
        Index("isArchived")
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceType: String = "TEXT", // TEXT, VOICE, IMAGE, SCREENSHOT, URL
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val originalInput: String = "",
    val mediaPath: String? = null,
    val mediaDurationMs: Long = 0L,
    val rawTranscript: String? = null,
    val cleanedTranscript: String? = null,
    val ocrText: String? = null,
    val webUrl: String? = null,
    val webTitle: String? = null,
    val webContent: String? = null,
    val title: String = "",
    val summary: String = "",
    val keyPoints: List<String> = emptyList(),
    val noteType: String = "THOUGHT", // THOUGHT, IDEA, REMINDER, TASK, FOLLOW_UP, REFERENCE, DECISION, QUESTION, MEETING, PURCHASE
    val intent: String = "Capture thought",
    val topics: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val people: List<String> = emptyList(),
    val projects: List<String> = emptyList(),
    val places: List<String> = emptyList(),
    val monetaryValues: List<String> = emptyList(),
    val importance: String = "NORMAL", // LOW, NORMAL, HIGH, URGENT
    val actionability: String = "NONE", // NONE, REMINDER, FOLLOW_UP, TASK
    val suggestedActions: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val processingState: String = "READY", // CAPTURED, EXTRACTING, TRANSCRIBING, UNDERSTANDING, READY, FAILED
    val processingError: String? = null,
    val userCorrected: Boolean = false
)
