package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkstationDao {

    // --- Transcription Records ---
    @Query("SELECT * FROM transcription_records ORDER BY timestamp DESC")
    fun getAllTranscriptions(): Flow<List<TranscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscription(record: TranscriptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTranscriptions(records: List<TranscriptionEntity>)

    @Update
    suspend fun updateTranscription(record: TranscriptionEntity)

    @Query("DELETE FROM transcription_records WHERE id = :id")
    suspend fun deleteTranscriptionById(id: Long)

    @Query("DELETE FROM transcription_records")
    suspend fun clearAllTranscriptions()

    // --- Code Snippets ---
    @Query("SELECT * FROM code_snippets ORDER BY timestamp DESC")
    fun getAllCodeSnippets(): Flow<List<CodeSnippetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodeSnippet(snippet: CodeSnippetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCodeSnippets(snippets: List<CodeSnippetEntity>)

    @Update
    suspend fun updateCodeSnippet(snippet: CodeSnippetEntity)

    @Query("DELETE FROM code_snippets WHERE id = :id")
    suspend fun deleteCodeSnippetById(id: Long)

    // --- Daily Stats ---
    @Query("SELECT * FROM daily_stats ORDER BY dateKey DESC")
    fun getAllStats(): Flow<List<DailyStatEntity>>

    @Query("SELECT * FROM daily_stats WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getStatForDate(dateKey: String): DailyStatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStat(stat: DailyStatEntity)
}
