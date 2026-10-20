package com.example.data.repository

import com.example.data.local.CodeSnippetEntity
import com.example.data.local.DailyStatEntity
import com.example.data.local.TranscriptionEntity
import com.example.data.local.WorkstationDao
import com.example.domain.engine.HindiTranscriptionEngine
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkstationRepository(private val dao: WorkstationDao) {

    val transcriptions: Flow<List<TranscriptionEntity>> = dao.getAllTranscriptions()
    val codeSnippets: Flow<List<CodeSnippetEntity>> = dao.getAllCodeSnippets()
    val dailyStats: Flow<List<DailyStatEntity>> = dao.getAllStats()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun getTodayKey(): String = dateFormat.format(Date())

    suspend fun saveTranscription(record: TranscriptionEntity): Long {
        return dao.insertTranscription(record)
    }

    suspend fun updateTranscription(record: TranscriptionEntity) {
        dao.updateTranscription(record)
    }

    suspend fun deleteTranscription(id: Long) {
        dao.deleteTranscriptionById(id)
    }

    suspend fun clearAllTranscriptions() {
        dao.clearAllTranscriptions()
    }

    suspend fun saveCodeSnippet(snippet: CodeSnippetEntity): Long {
        return dao.insertCodeSnippet(snippet)
    }

    suspend fun updateCodeSnippet(snippet: CodeSnippetEntity) {
        dao.updateCodeSnippet(snippet)
    }

    suspend fun deleteCodeSnippet(id: Long) {
        dao.deleteCodeSnippetById(id)
    }

    suspend fun recordEvaluationAction(approved: Boolean) {
        val today = getTodayKey()
        val current = dao.getStatForDate(today) ?: DailyStatEntity(dateKey = today)
        val updated = current.copy(
            evaluatedCount = current.evaluatedCount + 1,
            approvedCount = if (approved) current.approvedCount + 1 else current.approvedCount,
            rejectedCount = if (!approved) current.rejectedCount + 1 else current.rejectedCount
        )
        dao.insertOrUpdateStat(updated)
    }

    suspend fun recordCodeAuditAction() {
        val today = getTodayKey()
        val current = dao.getStatForDate(today) ?: DailyStatEntity(dateKey = today)
        val updated = current.copy(
            snippetsAudited = current.snippetsAudited + 1
        )
        dao.insertOrUpdateStat(updated)
    }

    suspend fun importTranscriptionsFromText(text: String, platform: String): Int {
        val trimmed = text.trim()
        val recordsToInsert = mutableListOf<TranscriptionEntity>()

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            // Parse as JSON array of objects or strings
            try {
                val jsonArray = JSONArray(trimmed)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.get(i)
                    if (item is JSONObject) {
                        val raw = item.optString("raw_text", item.optString("text", item.optString("rawHindi", "")))
                        val plat = item.optString("platform", platform)
                        if (raw.isNotBlank()) {
                            val analysis = HindiTranscriptionEngine.analyze(raw)
                            recordsToInsert.add(
                                TranscriptionEntity(
                                    platform = plat,
                                    rawText = raw,
                                    cleanText = analysis.cleanSuggestion,
                                    tagsDetected = analysis.detectedTags.joinToString(","),
                                    errorCount = analysis.issues.size,
                                    status = "PENDING"
                                )
                            )
                        }
                    } else if (item is String && item.isNotBlank()) {
                        val analysis = HindiTranscriptionEngine.analyze(item)
                        recordsToInsert.add(
                            TranscriptionEntity(
                                platform = platform,
                                rawText = item,
                                cleanText = analysis.cleanSuggestion,
                                tagsDetected = analysis.detectedTags.joinToString(","),
                                errorCount = analysis.issues.size,
                                status = "PENDING"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // fallback to line parsing
            }
        }

        if (recordsToInsert.isEmpty()) {
            // Parse as line-by-line or CSV
            val lines = trimmed.lines()
            val isCsvHeader = lines.firstOrNull()?.contains(",") == true &&
                    (lines.first().contains("raw", ignoreCase = true) || lines.first().contains("text", ignoreCase = true))

            val contentLines = if (isCsvHeader) lines.drop(1) else lines

            for (line in contentLines) {
                val cleanLine = line.trim()
                if (cleanLine.isNotBlank()) {
                    val rawText = if (cleanLine.contains(",")) {
                        cleanLine.split(",").last().trim().trim('"', '\'')
                    } else {
                        cleanLine
                    }

                    if (rawText.isNotBlank()) {
                        val analysis = HindiTranscriptionEngine.analyze(rawText)
                        recordsToInsert.add(
                            TranscriptionEntity(
                                platform = platform,
                                rawText = rawText,
                                cleanText = analysis.cleanSuggestion,
                                tagsDetected = analysis.detectedTags.joinToString(","),
                                errorCount = analysis.issues.size,
                                status = "PENDING"
                            )
                        )
                    }
                }
            }
        }

        if (recordsToInsert.isNotEmpty()) {
            dao.insertAllTranscriptions(recordsToInsert)
        }
        return recordsToInsert.size
    }

    suspend fun insertDefaultSamplesIfEmpty() {
        // Sample data will be loaded on demand or if table is empty
    }
}
