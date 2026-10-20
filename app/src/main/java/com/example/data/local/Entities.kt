package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcription_records")
data class TranscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platform: String = "Maven",
    val rawText: String,
    val cleanText: String,
    val tagsDetected: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val errorCount: Int = 0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "code_snippets")
data class CodeSnippetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val csharpCode: String,
    val category: String = "Concurrency",
    val safetyScore: Int = 100, // 0 - 100
    val issuesSummary: String = "",
    val aiAdvice: String = "",
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_stats")
data class DailyStatEntity(
    @PrimaryKey
    val dateKey: String, // e.g. "2026-10-20"
    val evaluatedCount: Int = 0,
    val approvedCount: Int = 0,
    val rejectedCount: Int = 0,
    val snippetsAudited: Int = 0
)
