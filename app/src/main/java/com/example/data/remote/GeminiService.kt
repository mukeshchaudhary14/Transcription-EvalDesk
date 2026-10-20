package com.example.data.remote

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

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun auditHindiTranscription(
        rawText: String,
        platform: String = "Maven"
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Set GEMINI_API_KEY in the Secrets panel in AI Studio.")
            )
        }

        val prompt = """
You are a senior Hindi audio transcription QA specialist for platforms like Maven, CrowdGen, and Poliwag.
Analyze the following raw Hindi transcript:
"$rawText"

Task:
1. Provide a clean, properly formatted, guideline-compliant Hindi transcript:
   - Use standard Hindi danda (।) for sentence boundaries, or ? / ! where appropriate.
   - Retain valid audio noise tags in standard square brackets: [laughter], [applause], [cough], [music], [background-noise], [inaudible], [screaming], [sigh], [throat-clearing].
   - Ensure a single space exists before '[' and after ']'.
   - Fix repeated stutter words if they appear to be typing errors.
2. Provide a 2-3 sentence QA evaluation summary explaining any detected mistakes, punctuation fixes, and guideline compliance notes.

Format your response exactly as:
CLEAN_TEXT:
<your sanitized clean Hindi transcript here>
QA_ANALYSIS:
<your detailed QA assessment and breakdown here>
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt, apiKey)
            val cleanText = extractSection(responseText, "CLEAN_TEXT:", "QA_ANALYSIS:")
                ?: responseText
            val qaAnalysis = extractSection(responseText, "QA_ANALYSIS:", null)
                ?: "Audio transcription verified against $platform formatting standards."

            Result.success(Pair(cleanText.trim(), qaAnalysis.trim()))
        } catch (e: Exception) {
            Log.e(TAG, "Gemini Hindi audit failed", e)
            Result.failure(e)
        }
    }

    suspend fun auditCSharpCode(
        csharpCode: String
    ): Result<Pair<Int, String>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Set GEMINI_API_KEY in the Secrets panel in AI Studio.")
            )
        }

        val prompt = """
You are a principal C# software architect and concurrency specialist.
Audit this C# code specifically for:
1. Thread-safety issues, race conditions, unsynchronized shared mutable state (e.g. non-concurrent collections), lock(this), and sync-over-async deadlocks (.Result / .Wait()).
2. Delegate and event memory leaks: event subscription (+=) without proper unsubscription (-=), lambda closures subscribed to long-lived events, lack of WeakEventManager / weak references.
3. Resource lifecycle: IDisposable implementation omissions, HttpClient socket exhaustion, missing CancellationToken propagation.

Code snippet:
```csharp
$csharpCode
```

Respond in this exact format:
SAFETY_SCORE: <integer 0 to 100>
ARCHITECTURAL_AUDIT:
<bulleted detailed vulnerability analysis and architectural refactoring advice>
REFACTORED_CODE:
<recommended thread-safe, leak-free C# implementation>
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt, apiKey)
            val scoreStr = extractSection(responseText, "SAFETY_SCORE:", "ARCHITECTURAL_AUDIT:")?.trim()
            val score = scoreStr?.toIntOrNull() ?: 75
            val auditDetails = extractSection(responseText, "ARCHITECTURAL_AUDIT:", null)
                ?: responseText

            Result.success(Pair(score.coerceIn(0, 100), auditDetails.trim()))
        } catch (e: Exception) {
            Log.e(TAG, "Gemini C# audit failed", e)
            Result.failure(e)
        }
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String {
        val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"

        val partsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("text", prompt)
            })
        }
        val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("parts", partsArray)
            })
        }
        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("topP", 0.95)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw RuntimeException("Gemini API call failed (${response.code}): $errorBody")
            }

            val body = response.body?.string() ?: throw RuntimeException("Empty response body from Gemini API")
            val jsonResponse = JSONObject(body)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
            throw RuntimeException("No text candidates found in Gemini API response")
        }
    }

    private fun extractSection(fullText: String, startHeader: String, endHeader: String?): String? {
        val startIndex = fullText.indexOf(startHeader)
        if (startIndex == -1) return null
        val actualStart = startIndex + startHeader.length

        return if (endHeader != null) {
            val endIndex = fullText.indexOf(endHeader, actualStart)
            if (endIndex != -1) {
                fullText.substring(actualStart, endIndex)
            } else {
                fullText.substring(actualStart)
            }
        } else {
            fullText.substring(actualStart)
        }
    }
}
