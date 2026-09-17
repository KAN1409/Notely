package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ExtractedNoteAnalysis
import com.example.ai.ExtractedUrlData
import com.example.ai.GeminiAiService
import com.example.ai.IntelligenceEngine
import com.example.ai.OcrEngine
import com.example.ai.OcrResult
import com.example.ai.UrlExtractor
import com.example.data.db.AppDatabase
import com.example.data.model.FollowUpEntity
import com.example.data.model.NoteEntity
import com.example.data.model.RelationshipEntity
import com.example.data.model.ReminderEntity
import com.example.data.repository.NoteRepository
import com.example.service.AudioPlayerHelper
import com.example.service.AudioRecorderHelper
import com.example.service.ReminderScheduler
import com.example.service.SpeechRecognitionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NotelyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = NoteRepository(database)
    private val reminderScheduler = ReminderScheduler(application)

    private val geminiService = GeminiAiService()
    val intelligenceEngine = IntelligenceEngine(geminiService)
    val ocrEngine = OcrEngine(application, geminiService)
    val urlExtractor = UrlExtractor()

    val recorderHelper = AudioRecorderHelper(application)
    val playerHelper = AudioPlayerHelper(application)
    val speechManager = SpeechRecognitionManager(application)

    // Navigation & View States
    val currentScreen = MutableStateFlow("SPLASH") // SPLASH, HOME, CAPTURE, RECORD, PROCESSING, NOTE_DETAIL, CONNECTIONS, KNOWLEDGE_GRAPH, FOLLOW_UPS, CALENDAR, SEARCH, OCR_DETAIL, LINK_DETAIL, EDIT_NOTE, SETTINGS, STATS
    val activeBottomTab = MutableStateFlow("HOME") // HOME, NOTES, SEARCH, STATS
    val selectedContextChip = MutableStateFlow("All") // All, Work, Personal, Health, Ideas
    val darkThemeEnabled = MutableStateFlow(true)

    // Notes Data
    val allNotes = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentNotes = repository.getRecentNotes(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedNoteId = MutableStateFlow<Long?>(null)

    val selectedNote = selectedNoteId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getNoteById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Search
    val searchQuery = MutableStateFlow("")
    val searchFilterType = MutableStateFlow("All") // All, Notes, Images, Links, People

    val searchResults = combine(searchQuery, searchFilterType, allNotes) { query, filter, notes ->
        if (query.isBlank()) {
            notes
        } else {
            val q = query.lowercase().trim()
            notes.filter { note ->
                val matchesQuery = note.title.lowercase().contains(q) ||
                        note.originalInput.lowercase().contains(q) ||
                        note.summary.lowercase().contains(q) ||
                        (note.ocrText?.lowercase()?.contains(q) == true) ||
                        (note.rawTranscript?.lowercase()?.contains(q) == true) ||
                        note.people.any { it.lowercase().contains(q) } ||
                        note.projects.any { it.lowercase().contains(q) } ||
                        note.topics.any { it.lowercase().contains(q) } ||
                        note.tags.any { it.lowercase().contains(q) }

                val matchesFilter = when (filter) {
                    "Notes" -> note.sourceType == "TEXT" || note.sourceType == "VOICE"
                    "Images" -> note.sourceType == "IMAGE" || note.sourceType == "SCREENSHOT"
                    "Links" -> note.sourceType == "URL"
                    "People" -> note.people.isNotEmpty()
                    else -> true
                }

                matchesQuery && matchesFilter
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Follow-ups & Reminders
    val allFollowUps = repository.getAllFollowUps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openFollowUps = repository.getOpenFollowUps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders = repository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders = repository.getActiveReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRelationships = repository.getAllRelationships()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Processing State
    val isProcessing = MutableStateFlow(false)
    val processingSteps = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    val processingTagline = MutableStateFlow("Turning your thoughts into useful knowledge.")

    // Audio & Transcription Live Preview
    val liveTranscriptLanguage = MutableStateFlow("Arabic • English (Auto)")
    val liveRecordingTranscript = MutableStateFlow("")

    // OCR & Link temp holders
    val pendingOcrResult = MutableStateFlow<OcrResult?>(null)
    val pendingUrlResult = MutableStateFlow<ExtractedUrlData?>(null)
    val pendingCapturedImageUri = MutableStateFlow<Uri?>(null)

    // Calendar selected date (millis)
    val selectedCalendarDate = MutableStateFlow(System.currentTimeMillis())

    // AI Copilot / Ask Notes State
    val askAiQuery = MutableStateFlow("")
    val askAiMessages = MutableStateFlow<List<com.example.ai.ChatMessage>>(
        listOf(
            com.example.ai.ChatMessage(
                sender = "COPILOT",
                message = "Hello! I am your NexNote Copilot. Ask me anything about your notes, meeting memos, receipts, action items, or upcoming deadlines."
            )
        )
    )
    val isAiThinking = MutableStateFlow(false)

    // Daily Executive Briefing State
    val dailyBriefingText = MutableStateFlow<String?>(null)
    val isGeneratingBriefing = MutableStateFlow(false)

    // Note Transform State (for NoteDetailScreen)
    val noteTransformState = MutableStateFlow(com.example.ai.NoteTransformState())

    init {
        // Ensure database is seeded on first run
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notes = repository.getAllNotesSync()
                if (notes.isEmpty()) {
                    AppDatabase.seedInitialData(database)
                }
            } catch (e: Exception) {
                android.util.Log.e("NotelyViewModel", "Error checking initial seed data", e)
            }
        }

        // Dismiss splash after brief delay
        viewModelScope.launch {
            delay(1200)
            if (currentScreen.value == "SPLASH") {
                currentScreen.value = "HOME"
            }
        }
    }

    fun navigateTo(screen: String) {
        currentScreen.value = screen
    }

    fun openNoteDetail(noteId: Long) {
        selectedNoteId.value = noteId
        currentScreen.value = "NOTE_DETAIL"
    }

    fun startRecording() {
        liveRecordingTranscript.value = ""
        speechManager.reset()
        val file = recorderHelper.startRecording()
        if (file != null) {
            currentScreen.value = "RECORD"
            // Start speech recognizer for live feedback
            speechManager.startListening(liveTranscriptLanguage.value)

            viewModelScope.launch {
                speechManager.liveText.collect { text ->
                    if (text.isNotBlank() && recorderHelper.isRecording.value) {
                        liveRecordingTranscript.value = text
                    }
                }
            }
        }
    }

    fun stopRecordingAndProcess() {
        val liveSpeech = speechManager.stopListening()
        val file = recorderHelper.stopRecording()
        val durationSeconds = recorderHelper.elapsedSeconds.value

        currentScreen.value = "PROCESSING"
        isProcessing.value = true

        viewModelScope.launch(Dispatchers.IO) {
            processingSteps.value = listOf(
                "Ingesting audio recording" to true,
                "Transcribing speech with AI" to false,
                "Extracting key facts & entities" to false,
                "Classifying intent & actionability" to false,
                "Finding connections with knowledge graph" to false,
                "Synthesizing structured note" to false
            )

            delay(200)
            updateStep(1, true)

            // Step 1: Real AI Audio Transcription from the recorded audio file using Gemini 3.6 Flash
            var transcript: String? = null
            if (file != null && file.exists() && file.length() > 500L) {
                try {
                    transcript = geminiService.transcribeAudio(file, liveTranscriptLanguage.value)
                } catch (e: Exception) {
                    android.util.Log.e("NotelyViewModel", "Gemini audio transcription error", e)
                }
            }

            // Step 2: Fallback to speech recognizer results if audio file transcription wasn't available
            if (transcript.isNullOrBlank()) {
                val liveFallback = liveSpeech.ifBlank { liveRecordingTranscript.value }.trim()
                if (liveFallback.isNotBlank()) {
                    transcript = liveFallback
                }
            }

            // Step 3: Clean final transcript
            val finalTranscript = if (!transcript.isNullOrBlank()) {
                transcript
            } else {
                val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                "Voice memo recorded on $dateStr (${durationSeconds}s)"
            }

            liveRecordingTranscript.value = finalTranscript
            delay(200)
            updateStep(2, true)

            val existing = repository.getAllNotesSync()
            val analysis = intelligenceEngine.processNote(finalTranscript, "VOICE", existing)

            delay(200)
            updateStep(3, true)
            delay(200)
            updateStep(4, true)
            delay(200)
            updateStep(5, true)

            val note = NoteEntity(
                sourceType = "VOICE",
                originalInput = finalTranscript,
                mediaPath = file?.absolutePath,
                mediaDurationMs = (durationSeconds * 1000L).coerceAtLeast(1000L),
                rawTranscript = finalTranscript,
                cleanedTranscript = analysis.summary,
                ocrText = null,
                webUrl = null,
                webTitle = null,
                webContent = null,
                title = analysis.title,
                summary = analysis.summary,
                keyPoints = analysis.keyPoints,
                noteType = analysis.noteType,
                intent = analysis.intent,
                topics = analysis.topics,
                tags = analysis.tags,
                people = analysis.people,
                projects = analysis.projects,
                places = analysis.places,
                monetaryValues = analysis.monetaryValues,
                importance = analysis.importance,
                actionability = analysis.actionability,
                suggestedActions = analysis.suggestedActions,
                processingState = "READY"
            )

            val newNoteId = repository.insertNote(note)

            // Save discovered relationships
            for (rel in analysis.relationshipsWithExisting) {
                repository.insertRelationship(
                    RelationshipEntity(
                        sourceNoteId = newNoteId,
                        targetNoteId = rel.targetNoteId,
                        relationshipType = rel.relationshipType,
                        explanation = rel.explanation
                    )
                )
            }

            // Save auto-generated follow-up if applicable
            if (analysis.actionability == "FOLLOW_UP" || analysis.suggestedFollowUpSubject != null) {
                repository.insertFollowUp(
                    FollowUpEntity(
                        noteId = newNoteId,
                        subject = analysis.suggestedFollowUpSubject ?: "${analysis.people.firstOrNull() ?: "Team"} - follow-up",
                        context = analysis.summary,
                        person = analysis.suggestedFollowUpPerson,
                        project = analysis.projects.firstOrNull(),
                        status = "OPEN",
                        dueDateMillis = analysis.suggestedFollowUpDueDate ?: (System.currentTimeMillis() + 86400000L)
                    )
                )
            }

            // Save auto-generated reminder if temporal intent found
            if (analysis.suggestedReminderTime != null) {
                val remId = repository.insertReminder(
                    ReminderEntity(
                        noteId = newNoteId,
                        title = analysis.title,
                        scheduledTimeMillis = analysis.suggestedReminderTime
                    )
                )
                reminderScheduler.schedule(
                    reminderId = remId,
                    noteId = newNoteId,
                    title = analysis.title,
                    timeMillis = analysis.suggestedReminderTime
                )
            }

            delay(300)
            isProcessing.value = false
            selectedNoteId.value = newNoteId
            currentScreen.value = "NOTE_DETAIL"
        }
    }

    fun processTextInput(text: String) {
        if (text.isBlank()) return
        processRawCapture(
            rawInput = text,
            sourceType = "TEXT"
        )
    }

    fun processImageCapture(uri: Uri?, userHint: String? = null) {
        pendingCapturedImageUri.value = uri
        viewModelScope.launch {
            val ocr = ocrEngine.extractFromImage(uri ?: Uri.EMPTY, userHint)
            pendingOcrResult.value = ocr
            processRawCapture(
                rawInput = ocr.fullText,
                sourceType = "IMAGE",
                ocrText = ocr.fullText
            )
        }
    }

    fun processUrlCapture(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            val data = urlExtractor.extract(url)
            pendingUrlResult.value = data
            processRawCapture(
                rawInput = data.fullText,
                sourceType = "URL",
                webUrl = data.url,
                webTitle = data.title,
                webContent = data.description
            )
        }
    }

    private fun processRawCapture(
        rawInput: String,
        sourceType: String,
        mediaPath: String? = null,
        mediaDurationMs: Long = 0L,
        rawTranscript: String? = null,
        ocrText: String? = null,
        webUrl: String? = null,
        webTitle: String? = null,
        webContent: String? = null
    ) {
        currentScreen.value = "PROCESSING"
        isProcessing.value = true

        viewModelScope.launch(Dispatchers.IO) {
            processingSteps.value = listOf(
                "Ingesting raw input" to true,
                "Transcribing & cleaning speech" to false,
                "Extracting key facts & entities" to false,
                "Classifying intent & actionability" to false,
                "Finding connections with knowledge graph" to false,
                "Synthesizing structured note" to false
            )

            delay(350)
            updateStep(1, true)
            delay(350)
            updateStep(2, true)

            val existing = repository.getAllNotesSync()
            val analysis = intelligenceEngine.processNote(rawInput, sourceType, existing)

            delay(300)
            updateStep(3, true)
            delay(300)
            updateStep(4, true)
            delay(300)
            updateStep(5, true)

            // Save note entity
            val note = NoteEntity(
                sourceType = sourceType,
                originalInput = rawInput,
                mediaPath = mediaPath,
                mediaDurationMs = mediaDurationMs,
                rawTranscript = rawTranscript,
                cleanedTranscript = if (sourceType == "VOICE") analysis.summary else null,
                ocrText = ocrText,
                webUrl = webUrl,
                webTitle = webTitle,
                webContent = webContent,
                title = analysis.title,
                summary = analysis.summary,
                keyPoints = analysis.keyPoints,
                noteType = analysis.noteType,
                intent = analysis.intent,
                topics = analysis.topics,
                tags = analysis.tags,
                people = analysis.people,
                projects = analysis.projects,
                places = analysis.places,
                monetaryValues = analysis.monetaryValues,
                importance = analysis.importance,
                actionability = analysis.actionability,
                suggestedActions = analysis.suggestedActions,
                processingState = "READY"
            )

            val newNoteId = repository.insertNote(note)

            // Save discovered relationships
            for (rel in analysis.relationshipsWithExisting) {
                repository.insertRelationship(
                    RelationshipEntity(
                        sourceNoteId = newNoteId,
                        targetNoteId = rel.targetNoteId,
                        relationshipType = rel.relationshipType,
                        explanation = rel.explanation
                    )
                )
            }

            // Save auto-generated follow-up if applicable
            if (analysis.actionability == "FOLLOW_UP" || analysis.suggestedFollowUpSubject != null) {
                repository.insertFollowUp(
                    FollowUpEntity(
                        noteId = newNoteId,
                        subject = analysis.suggestedFollowUpSubject ?: "${analysis.people.firstOrNull() ?: "Team"} - follow-up",
                        context = analysis.summary,
                        person = analysis.suggestedFollowUpPerson,
                        project = analysis.projects.firstOrNull(),
                        status = "OPEN",
                        dueDateMillis = analysis.suggestedFollowUpDueDate ?: (System.currentTimeMillis() + 86400000L)
                    )
                )
            }

            // Save auto-generated reminder if temporal intent found
            if (analysis.suggestedReminderTime != null) {
                val remId = repository.insertReminder(
                    ReminderEntity(
                        noteId = newNoteId,
                        title = analysis.title,
                        scheduledTimeMillis = analysis.suggestedReminderTime
                    )
                )
                reminderScheduler.schedule(
                    reminderId = remId,
                    noteId = newNoteId,
                    title = analysis.title,
                    timeMillis = analysis.suggestedReminderTime
                )
            }

            delay(200)
            isProcessing.value = false
            selectedNoteId.value = newNoteId
            currentScreen.value = "NOTE_DETAIL"
        }
    }

    private fun updateStep(index: Int, completed: Boolean) {
        val current = processingSteps.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].first to completed
            processingSteps.value = current
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
            if (selectedNoteId.value == id) {
                selectedNoteId.value = null
                currentScreen.value = "HOME"
            }
        }
    }

    fun toggleFollowUpStatus(followUp: FollowUpEntity) {
        viewModelScope.launch {
            val nextStatus = when (followUp.status) {
                "OPEN" -> "WAITING"
                "WAITING" -> "DUE"
                "DUE" -> "RESOLVED"
                "OVERDUE" -> "RESOLVED"
                else -> "OPEN"
            }
            repository.updateFollowUpStatus(followUp.id, nextStatus)
        }
    }

    fun resolveFollowUp(id: Long) {
        viewModelScope.launch {
            repository.updateFollowUpStatus(id, "RESOLVED")
        }
    }

    fun addFollowUp(noteId: Long, subject: String, person: String?, project: String?, dueDate: Long?) {
        viewModelScope.launch {
            repository.insertFollowUp(
                FollowUpEntity(
                    noteId = noteId,
                    subject = subject,
                    context = "Manual user follow up",
                    person = person,
                    project = project,
                    status = "OPEN",
                    dueDateMillis = dueDate
                )
            )
        }
    }

    fun toggleReminderCompleted(reminder: ReminderEntity) {
        viewModelScope.launch {
            val next = !reminder.isCompleted
            repository.setReminderCompleted(reminder.id, next)
            if (next) {
                reminderScheduler.cancel(reminder.id)
            }
        }
    }

    fun addReminder(noteId: Long, title: String, timeMillis: Long) {
        viewModelScope.launch {
            val id = repository.insertReminder(
                ReminderEntity(
                    noteId = noteId,
                    title = title,
                    scheduledTimeMillis = timeMillis
                )
            )
            reminderScheduler.schedule(id, noteId, title, timeMillis)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            reminderScheduler.cancel(id)
            repository.deleteReminder(id)
        }
    }

    fun askNotes(prompt: String) {
        val q = prompt.trim()
        if (q.isBlank() || isAiThinking.value) return

        val userMsg = com.example.ai.ChatMessage(
            sender = "USER",
            message = q
        )
        askAiMessages.value = askAiMessages.value + userMsg
        askAiQuery.value = ""
        isAiThinking.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val notes = repository.getAllNotesSync()
            val notesContext = buildString {
                notes.take(30).forEachIndexed { index, n ->
                    append("[#${n.id}] Title: ${n.title}\n")
                    append("Type: ${n.noteType} | Importance: ${n.importance}\n")
                    append("Summary: ${n.summary.ifBlank { n.originalInput }}\n")
                    if (n.people.isNotEmpty()) append("People: ${n.people.joinToString(", ")}\n")
                    if (n.projects.isNotEmpty()) append("Projects: ${n.projects.joinToString(", ")}\n")
                    if (n.suggestedActions.isNotEmpty()) append("Actions: ${n.suggestedActions.joinToString("; ")}\n")
                    append("\n")
                }
            }

            val answer = geminiService.answerQueryAcrossNotes(q, notesContext)
            isAiThinking.value = false

            val copilotMsg = com.example.ai.ChatMessage(
                sender = "COPILOT",
                message = answer
            )
            askAiMessages.value = askAiMessages.value + copilotMsg
        }
    }

    fun refreshDailyBriefing() {
        if (isGeneratingBriefing.value) return
        isGeneratingBriefing.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val notes = allNotes.value
            val followUps = allFollowUps.value
            val reminders = allReminders.value

            val notesContext = notes.take(15).joinToString("\n---\n") { note ->
                val sum = note.summary.ifBlank { note.originalInput }
                "${note.title}: $sum"
            }

            val pendingFollowUps = followUps.filter { it.status != "RESOLVED" }
            val followUpsContext = if (pendingFollowUps.isEmpty()) {
                "No open follow-ups"
            } else {
                pendingFollowUps.joinToString("\n") { f ->
                    "• ${f.subject} (Person: ${f.person ?: "N/A"}, Status: ${f.status})"
                }
            }

            val pendingReminders = reminders.filter { !it.isCompleted }
            val remindersContext = if (pendingReminders.isEmpty()) {
                "No scheduled reminders"
            } else {
                pendingReminders.joinToString("\n") { r ->
                    val dateStr = java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault()).format(java.util.Date(r.scheduledTimeMillis))
                    "• ${r.title} at $dateStr"
                }
            }

            val result = geminiService.generateDailyBriefing(notesContext, followUpsContext, remindersContext)
            dailyBriefingText.value = result
            isGeneratingBriefing.value = false
        }
    }

    fun performNoteTransform(noteId: Long, transformType: String) {
        val note = allNotes.value.find { it.id == noteId } ?: return
        noteTransformState.value = com.example.ai.NoteTransformState(
            isTransforming = true,
            transformType = transformType,
            resultText = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = geminiService.generateNoteTransform(
                transformType = transformType,
                noteTitle = note.title,
                noteContent = note.originalInput,
                summary = note.summary
            )
            noteTransformState.value = com.example.ai.NoteTransformState(
                isTransforming = false,
                transformType = transformType,
                resultText = result
            )
        }
    }

    fun clearTransformState() {
        noteTransformState.value = com.example.ai.NoteTransformState()
    }
}

