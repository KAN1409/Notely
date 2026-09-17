package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val apiKey = BuildConfig.GEMINI_API_KEY

    val isConfigured: Boolean
        get() = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

    suspend fun transcribeAudio(
        audioFile: java.io.File,
        languageHint: String? = null
    ): String? = withContext(Dispatchers.IO) {
        if (!isConfigured || !audioFile.exists() || audioFile.length() < 100L) {
            Log.w("GeminiAiService", "Cannot transcribe audio: configured=$isConfigured, exists=${audioFile.exists()}, size=${audioFile.length()}")
            return@withContext null
        }

        try {
            val audioBytes = audioFile.readBytes()
            val audioBase64 = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"

            val promptText = when {
                languageHint?.contains("Arabic", ignoreCase = true) == true ->
                    "Transcribe this speech audio verbatim in Arabic. Do not translate. Return ONLY the exact transcribed text, without markdown, timestamps, or quotes."
                languageHint?.contains("English", ignoreCase = true) == true ->
                    "Transcribe this speech audio verbatim in English. Return ONLY the exact transcribed text, without markdown, timestamps, or quotes."
                else ->
                    "Transcribe this speech audio verbatim in its original spoken language (Arabic, English, or mixed/code-switched). Do not translate. Return ONLY the exact transcribed text, without any timestamps, introductory text, explanations, or markdown. If there is no discernible speech or only silence/static, return [NO_SPEECH]."
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "audio/mp4")
                                    put("data", audioBase64)
                                })
                            })
                            put(JSONObject().put("text", promptText))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.0)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (!response.isSuccessful) {
                Log.e("GeminiAiService", "Gemini Audio Transcription Error: ${response.code} $responseBody")
                return@withContext null
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null
            val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext null
            val parts = contentObj.optJSONArray("parts") ?: return@withContext null
            if (parts.length() == 0) return@withContext null

            var resultText = parts.getJSONObject(0).optString("text").trim()
            resultText = resultText.removeSurrounding("\"").trim()

            if (resultText.isBlank() || resultText.equals("[NO_SPEECH]", ignoreCase = true)) {
                Log.d("GeminiAiService", "Gemini detected no speech in audio file.")
                return@withContext null
            }

            Log.d("GeminiAiService", "Gemini successfully transcribed audio: $resultText")
            resultText
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Failed to transcribe audio with Gemini", e)
            null
        }
    }

    suspend fun extractOcrFromImage(
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg",
        userHint: String? = null
    ): OcrResult? = withContext(Dispatchers.IO) {
        if (!isConfigured || imageBytes.isEmpty()) {
            Log.w("GeminiAiService", "Cannot run OCR: configured=$isConfigured, size=${imageBytes.size}")
            return@withContext null
        }

        try {
            val imageBase64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"

            val prompt = buildString {
                append("Analyze this image and perform complete optical character recognition (OCR).\n")
                append("Extract ALL text present in the image verbatim in its original language (Arabic, English, French, numbers, tabular data, handwritten text, or receipts).\n")
                if (!userHint.isNullOrBlank()) {
                    append("User context note: $userHint\n")
                }
                append("Identify the document type (RECEIPT, QUOTATION, INVOICE, DOCUMENT, NOTE, or CONTRACT).\n")
                append("Also extract vendor/supplier name, total amount/price, line items, and date if present.\n")
                append("Return ONLY JSON matching this schema:\n")
                append("{\n")
                append("  \"fullText\": \"All extracted text preserving linebreaks\",\n")
                append("  \"documentType\": \"RECEIPT\" | \"QUOTATION\" | \"INVOICE\" | \"DOCUMENT\" | \"NOTE\",\n")
                append("  \"supplier\": \"Vendor or company name or null\",\n")
                append("  \"totalAmount\": \"Total price or financial figure if any or null\",\n")
                append("  \"lineItems\": [\"item 1\", \"item 2\"],\n")
                append("  \"dateText\": \"Date found in document or null\"\n")
                append("}")
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", mimeType)
                                    put("data", imageBase64)
                                })
                            })
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("responseMimeType", "application/json")
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (!response.isSuccessful) {
                Log.e("GeminiAiService", "Gemini OCR Error: ${response.code} $responseBody")
                return@withContext null
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null
            val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext null
            val parts = contentObj.optJSONArray("parts") ?: return@withContext null
            if (parts.length() == 0) return@withContext null

            var rawJson = parts.getJSONObject(0).optString("text").trim()
            if (rawJson.startsWith("```json")) {
                rawJson = rawJson.removePrefix("```json").removeSuffix("```").trim()
            } else if (rawJson.startsWith("```")) {
                rawJson = rawJson.removePrefix("```").removeSuffix("```").trim()
            }

            val parsed = JSONObject(rawJson)
            val fullText = parsed.optString("fullText", "").trim()
            val docType = parsed.optString("documentType", "DOCUMENT").ifBlank { "DOCUMENT" }
            val supplier = parsed.optString("supplier", "").takeIf { it.isNotBlank() && it != "null" }
            val totalAmount = parsed.optString("totalAmount", "").takeIf { it.isNotBlank() && it != "null" }
            val dateText = parsed.optString("dateText", "").takeIf { it.isNotBlank() && it != "null" }
            val itemsJson = parsed.optJSONArray("lineItems")
            val items = mutableListOf<String>()
            if (itemsJson != null) {
                for (i in 0 until itemsJson.length()) {
                    val it = itemsJson.optString(i, "")
                    if (it.isNotBlank()) items.add(it)
                }
            }

            OcrResult(
                fullText = if (fullText.isNotBlank()) fullText else (userHint ?: "Document captured with no discernible text"),
                documentType = docType,
                supplier = supplier,
                totalAmount = totalAmount,
                lineItems = items,
                dateText = dateText
            )
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Failed to extract OCR with Gemini", e)
            null
        }
    }

    suspend fun analyzeContent(
        content: String,
        sourceType: String,
        existingNotesSummary: String
    ): ExtractedNoteAnalysis? = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("GeminiAiService", "API key not configured, will use local intelligence engine.")
            return@withContext null
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"

            val systemInstruction = """
                You are Notely AI, an expert note intelligence engine. 
                Your task is to analyze raw input (text, audio transcript, OCR receipt, or web article in Arabic, English, or mixed) and extract structured knowledge.
                Return ONLY valid JSON matching this schema:
                {
                   "title": "Short descriptive title (max 5 words)",
                   "summary": "Clear, grounded 1-2 sentence executive summary of what was stated",
                   "keyPoints": ["point 1", "point 2", ...],
                   "noteType": "THOUGHT|IDEA|REMINDER|TASK|FOLLOW_UP|REFERENCE|DECISION|QUESTION|MEETING|PURCHASE",
                   "intent": "What the user intended with this note",
                   "topics": ["topic1", "topic2"],
                   "tags": ["tag1", "tag2"],
                   "people": ["person name", ...],
                   "projects": ["project name", ...],
                   "places": ["location name", ...],
                   "monetaryValues": ["amount and currency", ...],
                   "importance": "NORMAL|HIGH|URGENT",
                   "actionability": "NONE|REMINDER|FOLLOW_UP|TASK",
                   "suggestedActions": ["Action 1", ...],
                   "followUpPerson": "Name if waiting for someone or null",
                   "followUpSubject": "Subject if waiting or null",
                   "hasReminder": false,
                   "reminderOffsetHours": 0
                }
                CRITICAL: Ground all facts strictly in the input. Never invent information.
            """.trimIndent()

            val userPrompt = """
                Input type: $sourceType
                Input Content:
                $content

                Existing Notes Context:
                $existingNotesSummary
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemInstruction\n\n$userPrompt"))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (!response.isSuccessful) {
                Log.e("GeminiAiService", "Gemini API error: ${response.code} $responseBody")
                return@withContext null
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null
            val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext null
            val parts = contentObj.optJSONArray("parts") ?: return@withContext null
            if (parts.length() == 0) return@withContext null
            var rawText = parts.getJSONObject(0).optString("text").trim()
            if (rawText.startsWith("```json")) {
                rawText = rawText.removePrefix("```json").removeSuffix("```").trim()
            } else if (rawText.startsWith("```")) {
                rawText = rawText.removePrefix("```").removeSuffix("```").trim()
            }

            val parsed = JSONObject(rawText)

            val keyPointsList = mutableListOf<String>()
            parsed.optJSONArray("keyPoints")?.let { arr ->
                for (i in 0 until arr.length()) keyPointsList.add(arr.getString(i))
            }

            val topicsList = mutableListOf<String>()
            parsed.optJSONArray("topics")?.let { arr ->
                for (i in 0 until arr.length()) topicsList.add(arr.getString(i))
            }

            val tagsList = mutableListOf<String>()
            parsed.optJSONArray("tags")?.let { arr ->
                for (i in 0 until arr.length()) tagsList.add(arr.getString(i))
            }

            val peopleList = mutableListOf<String>()
            parsed.optJSONArray("people")?.let { arr ->
                for (i in 0 until arr.length()) peopleList.add(arr.getString(i))
            }

            val projectsList = mutableListOf<String>()
            parsed.optJSONArray("projects")?.let { arr ->
                for (i in 0 until arr.length()) projectsList.add(arr.getString(i))
            }

            val placesList = mutableListOf<String>()
            parsed.optJSONArray("places")?.let { arr ->
                for (i in 0 until arr.length()) placesList.add(arr.getString(i))
            }

            val moneyList = mutableListOf<String>()
            parsed.optJSONArray("monetaryValues")?.let { arr ->
                for (i in 0 until arr.length()) moneyList.add(arr.getString(i))
            }

            val actionsList = mutableListOf<String>()
            parsed.optJSONArray("suggestedActions")?.let { arr ->
                for (i in 0 until arr.length()) actionsList.add(arr.getString(i))
            }

            val hasReminder = parsed.optBoolean("hasReminder", false)
            val offsetHours = parsed.optInt("reminderOffsetHours", 4)
            val reminderTime = if (hasReminder) System.currentTimeMillis() + (offsetHours * 3600000L) else null

            ExtractedNoteAnalysis(
                title = parsed.optString("title", "Captured Note"),
                summary = parsed.optString("summary", content.take(120)),
                keyPoints = if (keyPointsList.isNotEmpty()) keyPointsList else listOf("Captured directly on ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}"),
                noteType = parsed.optString("noteType", "THOUGHT"),
                intent = parsed.optString("intent", "Personal knowledge capture"),
                topics = topicsList,
                tags = tagsList,
                people = peopleList,
                projects = projectsList,
                places = placesList,
                monetaryValues = moneyList,
                importance = parsed.optString("importance", "NORMAL"),
                actionability = parsed.optString("actionability", "NONE"),
                suggestedActions = actionsList,
                suggestedReminderTime = reminderTime,
                suggestedFollowUpPerson = parsed.optString("followUpPerson").takeIf { it.isNotBlank() && it != "null" },
                suggestedFollowUpSubject = parsed.optString("followUpSubject").takeIf { it.isNotBlank() && it != "null" }
            )
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Failed to parse Gemini response", e)
            null
        }
    }

    suspend fun answerQueryAcrossNotes(
        query: String,
        notesContext: String
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext "NexNote Offline Intelligence: Based on your query '$query', here are the matching entries found in your local vault. (Connect Gemini API key in settings for full generative reasoning)."
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"
            val systemInstruction = """
                You are NexNote Copilot, an ultra-smart, concise executive AI assistant for the user's personal and professional notes.
                The user will ask you a question. Answer it accurately, strictly grounded in their notes context provided below.
                Include specific note titles, numbers, people, or deadlines when mentioned.
                Format with clean markdown bullets, clear headings if helpful, and bold key terms.
                If the requested information is not present in their notes, state politely what is available.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemInstruction\n\nUSER QUERY: $query\n\nUSER NOTES VAULT CONTEXT:\n$notesContext"))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext "Unable to generate answer at this time."

            if (!response.isSuccessful) {
                return@withContext "AI service encountered an issue (${response.code})."
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return@withContext "No response candidate found."
            if (candidates.length() == 0) return@withContext "No response generated."
            val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext "Empty content."
            val parts = contentObj.optJSONArray("parts") ?: return@withContext "Empty response."
            if (parts.length() == 0) return@withContext "Empty response."
            parts.getJSONObject(0).optString("text").trim()
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Failed to answer query with Gemini", e)
            "Could not reach Gemini AI: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    suspend fun generateDailyBriefing(
        notesContext: String,
        followUpsContext: String,
        remindersContext: String
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext "Good day! You have active notes, open follow-ups, and scheduled reminders in your vault. Review your open loops below."
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"
            val systemInstruction = """
                You are NexNote Executive Intelligence.
                Synthesize a sharp, 2-3 paragraph Executive Daily Briefing for today.
                Highlight:
                1. 🚨 Critical Priorities & Urgent Deadlines / Reminders
                2. 👥 Waiting-on & Open Loops (people and deliverables)
                3. 💡 Key Takeaway from recent captures
                Keep it polished, high-contrast, motivating, and strictly grounded in the user's data.
            """.trimIndent()

            val prompt = """
                $systemInstruction
                
                ACTIVE REMINDERS:
                $remindersContext
                
                OPEN FOLLOW-UPS:
                $followUpsContext
                
                RECENT NOTES & CAPTURES:
                $notesContext
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext "Daily briefing unavailable."

            if (!response.isSuccessful) return@withContext "Daily briefing service returned code ${response.code}."

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return@withContext "No candidates."
            if (candidates.length() == 0) return@withContext "No response."
            val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext "No content."
            val parts = contentObj.optJSONArray("parts") ?: return@withContext "Empty."
            parts.getJSONObject(0).optString("text").trim()
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Failed to generate daily briefing", e)
            "Review your pending follow-ups and schedule to stay on top of today's priorities."
        }
    }

    suspend fun generateNoteTransform(
        transformType: String,
        noteTitle: String,
        noteContent: String,
        summary: String
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext when (transformType) {
                "EMAIL" -> "Subject: Re: $noteTitle\n\nHi team,\n\nRegarding $noteTitle:\n$summary\n\nPlease let me know if you have questions.\nBest regards,"
                "ACTION_PLAN" -> "### Action Plan for $noteTitle\n1. Review initial requirements\n2. Execute immediate action items\n3. Follow up with stakeholders"
                "ARABIC" -> "ملخص الملاحظة: $noteTitle\n$summary"
                "CHECKLIST" -> "[] Review $noteTitle\n[] Verify next steps\n[] Confirm completion"
                else -> summary
            }
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"
            val prompt = when (transformType) {
                "EMAIL" -> "Draft a professional, ready-to-send email based on this note. Include Subject line, greeting, clear body, and signoff:\nTitle: $noteTitle\nContent: $noteContent\nSummary: $summary"
                "ACTION_PLAN" -> "Transform this note into a step-by-step Execution Action Plan with timeline and responsibilities:\nTitle: $noteTitle\nContent: $noteContent"
                "ARABIC" -> "Translate and summarize this note in professional, elegant Arabic (العربية الفصحى):\nTitle: $noteTitle\nContent: $noteContent"
                "CHECKLIST" -> "Extract a comprehensive checklist with checkboxes [ ] of every actionable task or verification point from this note:\nTitle: $noteTitle\nContent: $noteContent"
                "KEY_DECISIONS" -> "Extract all key decisions, agreements, financial numbers, and commitments made in this note:\nTitle: $noteTitle\nContent: $noteContent"
                else -> "Summarize this note in 3 concise bullet points:\nTitle: $noteTitle\nContent: $noteContent"
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext "Transform failed."

            if (!response.isSuccessful) return@withContext "Error performing action."

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return@withContext "No candidates."
            val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext "No content."
            val parts = contentObj.optJSONArray("parts") ?: return@withContext "Empty."
            parts.getJSONObject(0).optString("text").trim()
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Failed note transform", e)
            "Transform failed: ${e.localizedMessage}"
        }
    }
}
