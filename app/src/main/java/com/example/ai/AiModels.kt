package com.example.ai

data class ExtractedNoteAnalysis(
    val title: String,
    val summary: String,
    val keyPoints: List<String>,
    val noteType: String, // THOUGHT, IDEA, REMINDER, TASK, FOLLOW_UP, REFERENCE, DECISION, QUESTION, MEETING, PURCHASE
    val intent: String,
    val topics: List<String>,
    val tags: List<String>,
    val people: List<String>,
    val projects: List<String>,
    val places: List<String>,
    val monetaryValues: List<String>,
    val importance: String, // NORMAL, HIGH, URGENT
    val actionability: String, // NONE, REMINDER, FOLLOW_UP, TASK
    val suggestedActions: List<String>,
    val suggestedReminderTime: Long? = null,
    val suggestedFollowUpPerson: String? = null,
    val suggestedFollowUpSubject: String? = null,
    val suggestedFollowUpDueDate: Long? = null,
    val relationshipsWithExisting: List<SuggestedRelationship> = emptyList()
)

data class SuggestedRelationship(
    val targetNoteId: Long,
    val relationshipType: String,
    val explanation: String
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "COPILOT"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedNoteIds: List<Long> = emptyList()
)

data class NoteTransformState(
    val isTransforming: Boolean = false,
    val transformType: String? = null,
    val resultText: String? = null
)
