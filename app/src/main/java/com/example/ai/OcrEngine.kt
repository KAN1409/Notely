package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class OcrResult(
    val fullText: String,
    val documentType: String, // QUOTATION, RECEIPT, INVOICE, DOCUMENT, NOTE
    val supplier: String? = null,
    val totalAmount: String? = null,
    val lineItems: List<String> = emptyList(),
    val dateText: String? = null
)

class OcrEngine(
    private val context: Context,
    private val geminiService: GeminiAiService = GeminiAiService()
) {

    suspend fun extractFromImage(imageUri: Uri, userHint: String?): OcrResult = withContext(Dispatchers.IO) {
        // Read actual image bytes if Uri is provided
        val imageBytes = if (imageUri != Uri.EMPTY) {
            try {
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    input.readBytes()
                }
            } catch (e: Exception) {
                Log.e("OcrEngine", "Failed to read image from Uri: $imageUri", e)
                null
            }
        } else {
            null
        }

        // 1. Run Gemini 3.6 Flash multimodal OCR if image is available and service is configured
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            val compressed = compressImageIfNeeded(imageBytes)
            val mimeType = try {
                context.contentResolver.getType(imageUri) ?: "image/jpeg"
            } catch (_: Exception) {
                "image/jpeg"
            }

            val geminiResult = geminiService.extractOcrFromImage(
                imageBytes = compressed,
                mimeType = mimeType,
                userHint = userHint
            )

            if (geminiResult != null && geminiResult.fullText.isNotBlank()) {
                Log.d("OcrEngine", "Gemini OCR succeeded: type=${geminiResult.documentType}, textLength=${geminiResult.fullText.length}")
                return@withContext geminiResult
            }
        }

        // 2. Fallback when user provided manual text / hint or offline fallback
        val baseText = if (!userHint.isNullOrBlank()) {
            userHint
        } else {
            "Captured document / image (${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())})"
        }

        val lines = baseText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var supplier: String? = null
        var totalAmount: String? = null
        var dateText: String? = null
        val items = mutableListOf<String>()

        for (line in lines) {
            val lower = line.lowercase()
            if (lower.contains("supplier:") || lower.contains("vendor:") || lower.contains("company:")) {
                supplier = line.substringAfter(":").trim()
            }
            if (lower.contains("total") || lower.contains("amount") || lower.contains("egp") || lower.contains("le") || lower.contains("$")) {
                if (totalAmount == null) {
                    totalAmount = line
                }
            }
            if (lower.contains("item:") || lower.startsWith("-") || lower.startsWith("•")) {
                items.add(line.removePrefix("Item:").removePrefix("-").removePrefix("•").trim())
            }
            if (lower.contains("validity:") || lower.contains("date:")) {
                dateText = line.substringAfter(":").trim()
            }
        }

        val docType = when {
            baseText.contains("receipt", ignoreCase = true) -> "RECEIPT"
            baseText.contains("invoice", ignoreCase = true) -> "INVOICE"
            baseText.contains("quotation", ignoreCase = true) || baseText.contains("quote", ignoreCase = true) -> "QUOTATION"
            else -> "DOCUMENT"
        }

        OcrResult(
            fullText = baseText,
            documentType = docType,
            supplier = supplier,
            totalAmount = totalAmount,
            lineItems = items,
            dateText = dateText
        )
    }

    private fun compressImageIfNeeded(rawBytes: ByteArray): ByteArray {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)
            val maxDim = maxOf(options.outWidth, options.outHeight)
            var sampleSize = 1
            while (maxDim / sampleSize > 1920) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions) ?: return rawBytes
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            bitmap.recycle()
            out.toByteArray()
        } catch (e: Exception) {
            Log.w("OcrEngine", "Image compression failed, using raw bytes", e)
            rawBytes
        }
    }
}
