package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ExtractedUrlData(
    val url: String,
    val domain: String,
    val title: String,
    val description: String,
    val fullText: String
)

class UrlExtractor {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun extract(url: String): ExtractedUrlData = withContext(Dispatchers.IO) {
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }

        val domain = try {
            java.net.URI(cleanUrl).host ?: "web"
        } catch (_: Exception) {
            "web"
        }

        try {
            val request = Request.Builder()
                .url(cleanUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; Notely/1.0)")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""

            // Extract Title
            val titleMatcher = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE).matcher(html)
            val title = if (titleMatcher.find()) {
                titleMatcher.group(1)?.replace(Regex("&[a-zA-Z0-9#]+;"), " ")?.trim() ?: "Resource from $domain"
            } else {
                "Resource from $domain"
            }

            // Extract Meta Description or OpenGraph description
            val metaMatcher = Pattern.compile("<meta\\s+(?:name|property)=[\"'](?:description|og:description)[\"']\\s+content=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE).matcher(html)
            val ogAltMatcher = Pattern.compile("<meta\\s+content=[\"'](.*?)[\"']\\s+(?:name|property)=[\"'](?:description|og:description)[\"']", Pattern.CASE_INSENSITIVE).matcher(html)
            
            val description = if (metaMatcher.find()) {
                metaMatcher.group(1)?.replace(Regex("&[a-zA-Z0-9#]+;"), " ")?.trim() ?: ""
            } else if (ogAltMatcher.find()) {
                ogAltMatcher.group(1)?.replace(Regex("&[a-zA-Z0-9#]+;"), " ")?.trim() ?: ""
            } else {
                // Strip tags from body and extract first clean paragraph
                val bodySnippet = html.replace(Regex("<script[\\s\\S]*?</script>"), " ")
                    .replace(Regex("<style[\\s\\S]*?</style>"), " ")
                    .replace(Regex("<[^>]+>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                bodySnippet.take(300).ifBlank { "Online resource content from $domain" }
            }

            ExtractedUrlData(
                url = cleanUrl,
                domain = domain,
                title = title,
                description = description,
                fullText = "$title\n\n$description"
            )
        } catch (_: Exception) {
            // Offline or fallback handling derived strictly from the URL itself
            val fallbackTitle = "Resource: ${domain.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            val fallbackDesc = "Article and reference material saved from $cleanUrl."

            ExtractedUrlData(
                url = cleanUrl,
                domain = domain,
                title = fallbackTitle,
                description = fallbackDesc,
                fullText = "$fallbackTitle\n\n$fallbackDesc"
            )
        }
    }
}
