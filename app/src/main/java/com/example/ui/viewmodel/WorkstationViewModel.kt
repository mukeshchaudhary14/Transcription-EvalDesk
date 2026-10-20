package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CodeSnippetEntity
import com.example.data.local.DailyStatEntity
import com.example.data.local.TranscriptionEntity
import com.example.data.remote.GeminiService
import com.example.data.repository.WorkstationRepository
import com.example.data.sample.SampleData
import com.example.domain.engine.CSharpCodeSafetyEngine
import com.example.domain.engine.CodeSafetyAnalysisResult
import com.example.domain.engine.HindiTranscriptionEngine
import com.example.domain.engine.TranscriptionAnalysisResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab {
    EVAL_DESK,
    CODE_VAULT,
    DASHBOARD
}

data class EvalDeskUiState(
    val rawText: String = "",
    val cleanText: String = "",
    val platform: String = "Maven",
    val localAnalysis: TranscriptionAnalysisResult? = null,
    val geminiAnalysis: String? = null,
    val isGeminiLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val selectedRecordId: Long? = null
)

data class CodeVaultUiState(
    val title: String = "",
    val code: String = "",
    val category: String = "Concurrency",
    val filterCategory: String = "All",
    val localAnalysis: CodeSafetyAnalysisResult? = null,
    val geminiAdvice: String? = null,
    val isGeminiLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val selectedSnippetId: Long? = null
)

class WorkstationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkstationRepository = WorkstationRepository(
        AppDatabase.getDatabase(application).workstationDao()
    )
    private val prefs = application.getSharedPreferences("evaldesk_user_prefs", Context.MODE_PRIVATE)

    // Welcome Dialog State
    private val _showWelcomeDialog = MutableStateFlow(!prefs.getBoolean("has_seen_welcome_guide", false))
    val showWelcomeDialog: StateFlow<Boolean> = _showWelcomeDialog.asStateFlow()

    fun dismissWelcomeDialog() {
        prefs.edit().putBoolean("has_seen_welcome_guide", true).apply()
        _showWelcomeDialog.value = false
    }

    fun openWelcomeDialog() {
        _showWelcomeDialog.value = true
    }

    // Active Navigation
    private val _currentTab = MutableStateFlow(AppTab.EVAL_DESK)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- EvalDesk State ---
    private val _evalState = MutableStateFlow(EvalDeskUiState())
    val evalState: StateFlow<EvalDeskUiState> = _evalState.asStateFlow()

    val savedTranscriptions: StateFlow<List<TranscriptionEntity>> = repository.transcriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Code Vault State ---
    private val _vaultState = MutableStateFlow(CodeVaultUiState())
    val vaultState: StateFlow<CodeVaultUiState> = _vaultState.asStateFlow()

    val savedCodeSnippets: StateFlow<List<CodeSnippetEntity>> = repository.codeSnippets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Daily Stats ---
    val dailyStats: StateFlow<List<DailyStatEntity>> = repository.dailyStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- EvalDesk Actions ---

    fun onRawTextChanged(newText: String) {
        val analysis = if (newText.isNotBlank()) HindiTranscriptionEngine.analyze(newText) else null
        _evalState.update {
            it.copy(
                rawText = newText,
                cleanText = analysis?.cleanSuggestion.orEmpty(),
                localAnalysis = analysis,
                errorMessage = null
            )
        }
    }

    fun setPlatform(platform: String) {
        _evalState.update { it.copy(platform = platform) }
    }

    fun insertTag(tag: String) {
        val current = _evalState.value.rawText
        val tagToInsert = if (tag == "।") "।" else if (tag == "?") " ?" else " [$tag] "
        val updated = current + tagToInsert
        onRawTextChanged(updated)
    }

    fun applyCleanSuggestion() {
        val clean = _evalState.value.cleanText
        if (clean.isNotBlank()) {
            onRawTextChanged(clean)
            _evalState.update { it.copy(statusMessage = "Applied clean transcription formatting.") }
        }
    }

    fun runGeminiTranscriptionAudit() {
        val raw = _evalState.value.rawText
        if (raw.isBlank()) {
            _evalState.update { it.copy(errorMessage = "Please enter transcription text to audit.") }
            return
        }

        viewModelScope.launch {
            _evalState.update { it.copy(isGeminiLoading = true, errorMessage = null, geminiAnalysis = null) }
            val result = GeminiService.auditHindiTranscription(raw, _evalState.value.platform)
            result.fold(
                onSuccess = { (clean, analysis) ->
                    _evalState.update {
                        it.copy(
                            isGeminiLoading = false,
                            cleanText = clean,
                            geminiAnalysis = analysis,
                            statusMessage = "AI linguistic audit completed successfully."
                        )
                    }
                },
                onFailure = {
                    val local = HindiTranscriptionEngine.analyze(raw)
                    _evalState.update {
                        it.copy(
                            isGeminiLoading = false,
                            cleanText = local.cleanSuggestion,
                            geminiAnalysis = "Quality Assessment: Evaluated using standard ${it.platform} speech guidelines. " +
                                    if (local.issues.isEmpty()) "Transcript matches guideline format."
                                    else "Identified ${local.issues.size} formatting adjustments below.",
                            statusMessage = "Evaluated with speech guideline rules."
                        )
                    }
                }
            )
        }
    }

    fun approveCurrentTask() {
        val state = _evalState.value
        if (state.rawText.isBlank()) return

        viewModelScope.launch {
            repository.saveTranscription(
                TranscriptionEntity(
                    id = state.selectedRecordId ?: 0,
                    platform = state.platform,
                    rawText = state.rawText,
                    cleanText = state.cleanText.ifBlank { state.rawText },
                    tagsDetected = state.localAnalysis?.detectedTags?.joinToString(",") ?: "",
                    status = "APPROVED",
                    errorCount = state.localAnalysis?.issues?.size ?: 0,
                    notes = state.geminiAnalysis ?: "Approved"
                )
            )
            repository.recordEvaluationAction(approved = true)
            _evalState.update {
                it.copy(
                    statusMessage = "Task approved and logged in throughput stats.",
                    selectedRecordId = null
                )
            }
        }
    }

    fun rejectCurrentTask(reason: String = "Formatting/Punctuation Violation") {
        val state = _evalState.value
        if (state.rawText.isBlank()) return

        viewModelScope.launch {
            repository.saveTranscription(
                TranscriptionEntity(
                    id = state.selectedRecordId ?: 0,
                    platform = state.platform,
                    rawText = state.rawText,
                    cleanText = state.cleanText.ifBlank { state.rawText },
                    tagsDetected = state.localAnalysis?.detectedTags?.joinToString(",") ?: "",
                    status = "REJECTED",
                    errorCount = state.localAnalysis?.issues?.size ?: 0,
                    notes = reason
                )
            )
            repository.recordEvaluationAction(approved = false)
            _evalState.update {
                it.copy(
                    statusMessage = "Task marked as rejected and logged.",
                    selectedRecordId = null
                )
            }
        }
    }

    fun loadTranscriptionIntoEditor(record: TranscriptionEntity) {
        val analysis = HindiTranscriptionEngine.analyze(record.rawText)
        _evalState.update {
            it.copy(
                rawText = record.rawText,
                cleanText = if (record.cleanText.isNotBlank()) record.cleanText else analysis.cleanSuggestion,
                platform = record.platform,
                localAnalysis = analysis,
                geminiAnalysis = if (record.notes.isNotBlank()) record.notes else null,
                selectedRecordId = if (record.id != 0L) record.id else null,
                errorMessage = null,
                statusMessage = "Loaded: ${record.platform} task"
            )
        }
    }

    fun loadSampleBatchTasks() {
        viewModelScope.launch {
            SampleData.sampleTranscriptions.forEach {
                repository.saveTranscription(it)
            }
            // Load the first one into view
            loadTranscriptionIntoEditor(SampleData.sampleTranscriptions.first())
            _evalState.update { it.copy(statusMessage = "Loaded practice evaluation tasks into queue.") }
        }
    }

    fun importBatchData(text: String, platform: String) {
        viewModelScope.launch {
            val count = repository.importTranscriptionsFromText(text, platform)
            _evalState.update {
                it.copy(statusMessage = "Successfully imported $count tasks into review queue.")
            }
        }
    }

    fun deleteTranscriptionRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteTranscription(id)
            if (_evalState.value.selectedRecordId == id) {
                _evalState.update { it.copy(selectedRecordId = null) }
            }
        }
    }

    fun clearAllQueue() {
        viewModelScope.launch {
            repository.clearAllTranscriptions()
            _evalState.update { it.copy(selectedRecordId = null, statusMessage = "Queue cleared.") }
        }
    }

    // --- Code Vault Actions ---

    fun onCodeChanged(newCode: String) {
        val analysis = if (newCode.isNotBlank()) CSharpCodeSafetyEngine.analyze(newCode) else null
        _vaultState.update {
            it.copy(
                code = newCode,
                localAnalysis = analysis,
                errorMessage = null
            )
        }
    }

    fun onCodeTitleChanged(newTitle: String) {
        _vaultState.update { it.copy(title = newTitle) }
    }

    fun setCodeCategory(category: String) {
        _vaultState.update { it.copy(category = category) }
    }

    fun setFilterCategory(category: String) {
        _vaultState.update { it.copy(filterCategory = category) }
    }

    fun runGeminiCodeAudit() {
        val code = _vaultState.value.code
        if (code.isBlank()) {
            _vaultState.update { it.copy(errorMessage = "Please enter C# code to scan.") }
            return
        }

        viewModelScope.launch {
            _vaultState.update { it.copy(isGeminiLoading = true, errorMessage = null, geminiAdvice = null) }
            val result = GeminiService.auditCSharpCode(code)
            result.fold(
                onSuccess = { (score, advice) ->
                    _vaultState.update {
                        it.copy(
                            isGeminiLoading = false,
                            geminiAdvice = advice,
                            statusMessage = "Concurrency audit completed (Safety: $score/100)."
                        )
                    }
                    repository.recordCodeAuditAction()
                },
                onFailure = {
                    val local = CSharpCodeSafetyEngine.analyze(code)
                    _vaultState.update {
                        it.copy(
                            isGeminiLoading = false,
                            geminiAdvice = "Static Safety Analysis (Score: ${local.score}/100):\n" +
                                    if (local.issues.isEmpty()) "• No concurrency or delegate leak hazards detected."
                                    else local.issues.joinToString("\n\n") { issue ->
                                        "• [${issue.severity}] ${issue.title}: ${issue.description}\nRecommended: ${issue.recommendation}"
                                    },
                            statusMessage = "Completed static safety analysis."
                        )
                    }
                    repository.recordCodeAuditAction()
                }
            )
        }
    }

    fun saveCurrentCodeSnippet() {
        val state = _vaultState.value
        if (state.code.isBlank()) return

        val title = if (state.title.isNotBlank()) state.title else "C# Pattern (${state.category})"
        val analysis = state.localAnalysis ?: CSharpCodeSafetyEngine.analyze(state.code)

        viewModelScope.launch {
            repository.saveCodeSnippet(
                CodeSnippetEntity(
                    id = state.selectedSnippetId ?: 0,
                    title = title,
                    csharpCode = state.code,
                    category = state.category,
                    safetyScore = analysis.score,
                    issuesSummary = analysis.issues.joinToString("; ") { it.title },
                    aiAdvice = state.geminiAdvice ?: "Audited with ${analysis.issues.size} rule findings"
                )
            )
            repository.recordCodeAuditAction()
            _vaultState.update {
                it.copy(
                    statusMessage = "Saved snippet to Code Vault.",
                    selectedSnippetId = null
                )
            }
        }
    }

    fun loadCodeIntoEditor(snippet: CodeSnippetEntity) {
        val analysis = CSharpCodeSafetyEngine.analyze(snippet.csharpCode)
        _vaultState.update {
            it.copy(
                title = snippet.title,
                code = snippet.csharpCode,
                category = snippet.category,
                localAnalysis = analysis,
                geminiAdvice = if (snippet.aiAdvice.isNotBlank()) snippet.aiAdvice else null,
                selectedSnippetId = if (snippet.id != 0L) snippet.id else null,
                errorMessage = null,
                statusMessage = "Loaded: ${snippet.title}"
            )
        }
    }

    fun loadSampleCodeSnippets() {
        viewModelScope.launch {
            SampleData.sampleCodeSnippets.forEach {
                repository.saveCodeSnippet(it)
            }
            loadCodeIntoEditor(SampleData.sampleCodeSnippets.first())
            _vaultState.update { it.copy(statusMessage = "Loaded C# architectural pattern presets.") }
        }
    }

    fun deleteCodeSnippet(id: Long) {
        viewModelScope.launch {
            repository.deleteCodeSnippet(id)
            if (_vaultState.value.selectedSnippetId == id) {
                _vaultState.update { it.copy(selectedSnippetId = null) }
            }
        }
    }

    fun dismissStatusMessage() {
        _evalState.update { it.copy(statusMessage = null, errorMessage = null) }
        _vaultState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
