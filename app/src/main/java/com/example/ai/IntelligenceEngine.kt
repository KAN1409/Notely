package com.example.ai

import com.example.data.model.NoteEntity
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

class IntelligenceEngine(private val geminiService: GeminiAiService) {

    suspend fun processNote(
        rawContent: String,
        sourceType: String,
        existingNotes: List<NoteEntity>
    ): ExtractedNoteAnalysis {
        // First try cloud Gemini AI if configured
        val contextSummary = existingNotes.take(5).joinToString("\n") {
            "- ID ${it.id}: [${it.title}] (People: ${it.people.joinToString()}, Project: ${it.projects.joinToString()}, Topics: ${it.topics.joinToString()})"
        }

        val cloudResult = geminiService.analyzeContent(rawContent, sourceType, contextSummary)
        val baseAnalysis = cloudResult ?: runLocalSemanticAnalysis(rawContent, sourceType)

        // Find connections with existing notes
        val relationships = findConnections(baseAnalysis, existingNotes)

        return baseAnalysis.copy(relationshipsWithExisting = relationships)
    }

    fun runLocalSemanticAnalysis(content: String, sourceType: String): ExtractedNoteAnalysis {
        val lower = content.lowercase(Locale.ROOT)

        // Detect People (Dynamic NER with Arabic & English patterns + common titles)
        val detectedPeople = mutableListOf<String>()
        val peoplePatterns = listOf(
            Regex("""\b(ahmed|أحمد)\b""", RegexOption.IGNORE_CASE) to "Ahmed",
            Regex("""\b(yasser|ياسر)\b""", RegexOption.IGNORE_CASE) to "Yasser",
            Regex("""\b(tarek|طارق|دكتور طارق|dr\.?\s*tarek)\b""", RegexOption.IGNORE_CASE) to "Dr. Tarek",
            Regex("""\b(karim|كريم)\b""", RegexOption.IGNORE_CASE) to "Karim",
            Regex("""\b(sara|سارة)\b""", RegexOption.IGNORE_CASE) to "Sara",
            Regex("""\b(mohamed|محمد|محمود)\b""", RegexOption.IGNORE_CASE) to "Mohamed",
            Regex("""\b(omar|عمر)\b""", RegexOption.IGNORE_CASE) to "Omar",
            Regex("""\b(khaled|خالد)\b""", RegexOption.IGNORE_CASE) to "Khaled",
            Regex("""\b(nour|نور)\b""", RegexOption.IGNORE_CASE) to "Nour",
            Regex("""\b(mona|منى)\b""", RegexOption.IGNORE_CASE) to "Mona",
            Regex("""\b(layla|ليلى)\b""", RegexOption.IGNORE_CASE) to "Layla",
            Regex("""\b(hassan|حسن)\b""", RegexOption.IGNORE_CASE) to "Hassan",
            Regex("""\b(mostafa|مصطفى)\b""", RegexOption.IGNORE_CASE) to "Mostafa",
            Regex("""\b(eng\.?\s*([A-Za-z]+)|مهندس\s*([\u0621-\u064A]+))\b""", RegexOption.IGNORE_CASE) to "Eng."
        )
        for ((pattern, canonical) in peoplePatterns) {
            val match = pattern.find(content)
            if (match != null) {
                val name = if (canonical == "Eng.") {
                    val group = match.groups[2]?.value ?: match.groups[3]?.value
                    if (!group.isNullOrBlank()) "Eng. ${group.trim().replaceFirstChar { it.uppercase() }}" else canonical
                } else canonical
                if (!detectedPeople.contains(name)) detectedPeople.add(name)
            }
        }

        // Dynamic Name Detection: "with [Name]", "مع [الاسم]", "contact [Name]", "call [Name]"
        val contactMatch = Regex("""\b(?:with|call|contact|talk to|speak to|مع|كلم|تواصل مع)\s+([A-Z][a-z]+|[\u0621-\u064A]{3,12})\b""").find(content)
        if (contactMatch != null) {
            val extractedName = contactMatch.groups[1]?.value?.trim()
            if (!extractedName.isNullOrBlank() && extractedName.length in 3..15) {
                val formatted = extractedName.replaceFirstChar { it.uppercase() }
                if (!detectedPeople.contains(formatted) && !listOf("The", "And", "For", "With", "All").contains(formatted)) {
                    detectedPeople.add(formatted)
                }
            }
        }

        // Detect Projects (Explicit and Pattern-based)
        val detectedProjects = mutableListOf<String>()
        val projectRegex = Regex("""\b(?:project|مشروع)\s+([A-Za-z0-9\-_]+|[\u0621-\u064A0-9]+)\b""", RegexOption.IGNORE_CASE)
        val projMatch = projectRegex.find(content)
        if (projMatch != null) {
            val pName = projMatch.groups[1]?.value?.trim()
            if (!pName.isNullOrBlank()) {
                detectedProjects.add("${pName.replaceFirstChar { it.uppercase() }} Project")
            }
        }
        if (lower.contains("negma") || lower.contains("نجمة") || lower.contains("site visit")) {
            if (!detectedProjects.any { it.contains("Negma", ignoreCase = true) }) detectedProjects.add("Negma Project")
        }
        if (lower.contains("medical center") || lower.contains("المركز الطبي") || lower.contains("pr 0262") || lower.contains("pr0262")) {
            if (!detectedProjects.any { it.contains("Medical", ignoreCase = true) }) detectedProjects.add("Medical Center")
        }
        if (lower.contains("hotel") || lower.contains("فندق") || lower.contains("hilton")) {
            if (!detectedProjects.contains("Negma Project") && !detectedProjects.any { it.contains("Hotel", ignoreCase = true) }) detectedProjects.add("Hotel Project")
        }

        // Detect Topics
        val detectedTopics = mutableListOf<String>()
        val topicKeywords = mapOf(
            "Lighting" to listOf("light", "lighting", "إضاءة", "مخفية", "سقف", "led", "cove"),
            "Marble" to listOf("marble", "رخام", "galala", "جرانيت", "بلاط", "stone"),
            "Quotation" to listOf("quotation", "quote", "عرض أسعار", "سعر", "price", "egp", "cost"),
            "Meeting" to listOf("meeting", "اجتماع", "مناقشة", "جلسة", "call"),
            "Design" to listOf("design", "تصميم", "mood board", "japandi", "مخطط", "ألوان"),
            "Health" to listOf("doctor", "دكتور", "appointment", "كشف", "عيادة", "hospital", "مستشفى", "تحاليل"),
            "Approval" to listOf("approval", "signoff", "موافقة", "اعتماد", "توقيع"),
            "Procurement" to listOf("supplier", "مورد", "order", "توريد", "كميات", "boq")
        )
        for ((topic, keywords) in topicKeywords) {
            if (keywords.any { lower.contains(it) }) {
                detectedTopics.add(topic)
            }
        }

        // Detect Money / Monetary Values
        val detectedMoney = mutableListOf<String>()
        val moneyRegex = Regex("""(\d+[\d,.]*\s*(?:egp|le|جنية|جنيه|usd|\$|euro|€)(?:\s*/\s*m[²2])?)""", RegexOption.IGNORE_CASE)
        val matches = moneyRegex.findAll(content)
        for (m in matches) {
            val valStr = m.value.trim()
            if (!detectedMoney.contains(valStr)) detectedMoney.add(valStr)
        }

        // Detect Dates and Temporal Intent
        val (suggestedReminderTime, temporalLabel) = detectTemporalIntent(content)

        // Classify Note Type & Intent
        val noteType: String
        val importance: String
        val actionability: String

        if (lower.contains("doctor") || lower.contains("appointment") || lower.contains("كشف") || lower.contains("تذكير") || lower.contains("remind")) {
            noteType = "REMINDER"
            importance = "HIGH"
            actionability = "REMINDER"
        } else if (lower.contains("quotation") || lower.contains("عرض أسعار") || lower.contains("egp") || lower.contains("شراء")) {
            noteType = "PURCHASE"
            importance = "HIGH"
            actionability = "FOLLOW_UP"
        } else if (lower.contains("waiting") || lower.contains("approval") || lower.contains("موافقة") || lower.contains("متابعة") || lower.contains("follow up")) {
            noteType = "FOLLOW_UP"
            importance = "HIGH"
            actionability = "FOLLOW_UP"
        } else if (sourceType == "VOICE" && (lower.contains("meeting") || lower.contains("site visit") || lower.contains("اتكلمنا") || lower.contains("ناقشنا"))) {
            noteType = "MEETING"
            importance = "HIGH"
            actionability = "FOLLOW_UP"
        } else if (sourceType == "URL") {
            noteType = "REFERENCE"
            importance = "NORMAL"
            actionability = "NONE"
        } else if (lower.contains("idea") || lower.contains("فكرة")) {
            noteType = "IDEA"
            importance = "NORMAL"
            actionability = "TASK"
        } else {
            noteType = "THOUGHT"
            importance = "NORMAL"
            actionability = if (suggestedReminderTime != null) "REMINDER" else "NONE"
        }

        // Generate Title
        val title = generateCleanTitle(content, detectedTopics, detectedPeople, detectedProjects, sourceType)

        // Generate Summary
        val summary = generateCleanSummary(content, detectedPeople, detectedProjects, detectedTopics, noteType)

        // Extract Key Points
        val keyPoints = extractKeyPoints(content, detectedTopics, detectedMoney)

        // Suggested Actions
        val suggestedActions = mutableListOf<String>()
        if (detectedPeople.isNotEmpty() && (noteType == "FOLLOW_UP" || noteType == "MEETING" || noteType == "PURCHASE")) {
            suggestedActions.add("Create follow-up with ${detectedPeople.first()}")
        }
        if (suggestedReminderTime != null) {
            suggestedActions.add("Set reminder for $temporalLabel")
        }
        if (detectedMoney.isNotEmpty()) {
            suggestedActions.add("Add to project budget tracker")
        }
        if (suggestedActions.isEmpty()) {
            suggestedActions.add("Organize in project tags")
        }

        return ExtractedNoteAnalysis(
            title = title,
            summary = summary,
            keyPoints = keyPoints,
            noteType = noteType,
            intent = "Capture and organize $noteType about ${detectedTopics.joinToString().ifEmpty { "insights" }}",
            topics = if (detectedTopics.isNotEmpty()) detectedTopics else listOf("General"),
            tags = (detectedTopics + detectedProjects).distinct(),
            people = detectedPeople,
            projects = detectedProjects,
            places = if (lower.contains("reception") || lower.contains("ريسبشن")) listOf("Reception Area") else emptyList(),
            monetaryValues = detectedMoney,
            importance = importance,
            actionability = actionability,
            suggestedActions = suggestedActions,
            suggestedReminderTime = suggestedReminderTime,
            suggestedFollowUpPerson = detectedPeople.firstOrNull(),
            suggestedFollowUpSubject = if (detectedTopics.isNotEmpty()) "${detectedTopics.first()} discussion" else title,
            suggestedFollowUpDueDate = if (suggestedReminderTime != null) suggestedReminderTime + 86400000L else null
        )
    }

    private fun detectTemporalIntent(content: String): Pair<Long?, String> {
        val lower = content.lowercase(Locale.ROOT)
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        return when {
            lower.contains("بكرة") || lower.contains("غدا") || lower.contains("غداً") || lower.contains("tomorrow") -> {
                cal.timeInMillis = now + 86400000L
                cal.set(Calendar.HOUR_OF_DAY, 10)
                cal.set(Calendar.MINUTE, 0)
                Pair(cal.timeInMillis, "Tomorrow at 10:00 AM")
            }
            lower.contains("اليوم") || lower.contains("today") || lower.contains("النهاردة") -> {
                cal.timeInMillis = now + (4 * 3600 * 1000)
                Pair(cal.timeInMillis, "Later Today")
            }
            lower.contains("الاتنين") || lower.contains("monday") -> {
                cal.add(Calendar.DAY_OF_YEAR, 2)
                cal.set(Calendar.HOUR_OF_DAY, 10)
                cal.set(Calendar.MINUTE, 0)
                Pair(cal.timeInMillis, "Monday at 10:00 AM")
            }
            lower.contains("الاربع") || lower.contains("الأربعاء") || lower.contains("wednesday") -> {
                cal.add(Calendar.DAY_OF_YEAR, 3)
                cal.set(Calendar.HOUR_OF_DAY, 13)
                cal.set(Calendar.MINUTE, 0)
                Pair(cal.timeInMillis, "Wednesday at 1:00 PM")
            }
            lower.contains("الخميس") || lower.contains("thursday") -> {
                cal.add(Calendar.DAY_OF_YEAR, 4)
                cal.set(Calendar.HOUR_OF_DAY, 15)
                Pair(cal.timeInMillis, "Thursday by end of day")
            }
            else -> Pair(null, "")
        }
    }

    private fun generateCleanTitle(
        content: String,
        topics: List<String>,
        people: List<String>,
        projects: List<String>,
        sourceType: String
    ): String {
        val firstLine = content.lines().firstOrNull { it.isNotBlank() }?.trim() ?: ""
        if (firstLine.length in 4..35 && !firstLine.startsWith("http")) {
            return firstLine
        }

        val personPart = people.firstOrNull()?.let { "$it - " } ?: ""
        val projectPart = projects.firstOrNull()
        val topicPart = topics.take(2).joinToString(" & ")

        return when {
            projectPart != null && topicPart.isNotBlank() -> "$projectPart: $topicPart"
            personPart.isNotBlank() && topicPart.isNotBlank() -> "$personPart$topicPart"
            topicPart.isNotBlank() -> topicPart
            sourceType == "VOICE" -> "Voice recording note"
            sourceType == "IMAGE" -> "Document quotation"
            sourceType == "URL" -> "Web resource reference"
            else -> "Quick personal note"
        }
    }

    private fun generateCleanSummary(
        content: String,
        people: List<String>,
        projects: List<String>,
        topics: List<String>,
        noteType: String
    ): String {
        val peopleStr = if (people.isNotEmpty()) " with ${people.joinToString(" and ")}" else ""
        val projectStr = if (projects.isNotEmpty()) " for ${projects.joinToString(" and ")}" else ""
        val topicStr = if (topics.isNotEmpty()) " regarding ${topics.joinToString(", ")}" else ""

        val cleaned = content.replace("\n", " ").trim()
        if (cleaned.length in 20..150) {
            return cleaned
        }

        return "Discussion and notes$peopleStr$projectStr$topicStr. Key details recorded for review and execution."
    }

    private fun extractKeyPoints(content: String, topics: List<String>, money: List<String>): List<String> {
        val lines = content.lines().map { it.trim() }.filter { it.length > 5 }
        val points = mutableListOf<String>()

        for (line in lines) {
            if (line.startsWith("-") || line.startsWith("•") || line.startsWith("*")) {
                points.add(line.trimStart('-', '•', '*', ' '))
            }
        }

        if (money.isNotEmpty()) {
            points.add("Financial: ${money.joinToString(" • ")}")
        }

        if (points.isEmpty()) {
            val sentences = content.split(Regex("""[.!?؛\n]+""")).map { it.trim() }.filter { it.length > 8 }
            points.addAll(sentences.take(3))
        }

        if (points.isEmpty()) {
            points.add("Recorded input received and filed into knowledge base")
        }

        return points.take(4)
    }

    private fun findConnections(
        analysis: ExtractedNoteAnalysis,
        existingNotes: List<NoteEntity>
    ): List<SuggestedRelationship> {
        val relationships = mutableListOf<SuggestedRelationship>()

        for (note in existingNotes) {
            // Check project match
            val commonProjects = analysis.projects.intersect(note.projects.toSet())
            if (commonProjects.isNotEmpty()) {
                relationships.add(
                    SuggestedRelationship(
                        targetNoteId = note.id,
                        relationshipType = "SAME_PROJECT",
                        explanation = "Both relate to ${commonProjects.joinToString()} (${note.title})"
                    )
                )
                continue
            }

            // Check person match
            val commonPeople = analysis.people.intersect(note.people.toSet())
            if (commonPeople.isNotEmpty()) {
                relationships.add(
                    SuggestedRelationship(
                        targetNoteId = note.id,
                        relationshipType = "SAME_PERSON",
                        explanation = "Both involve ${commonPeople.joinToString()} (${note.title})"
                    )
                )
                continue
            }

            // Check topic match
            val commonTopics = analysis.topics.intersect(note.topics.toSet())
            if (commonTopics.size >= 2) {
                relationships.add(
                    SuggestedRelationship(
                        targetNoteId = note.id,
                        relationshipType = "SAME_TOPIC",
                        explanation = "Both share themes of ${commonTopics.joinToString()} (${note.title})"
                    )
                )
            }
        }

        return relationships.take(3)
    }
}
