package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TranscriptionEntity
import com.example.domain.engine.HindiTranscriptionEngine
import com.example.domain.engine.Severity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.EvalDeskUiState

@Composable
fun EvalDeskScreen(
    state: EvalDeskUiState,
    savedTasks: List<TranscriptionEntity>,
    onRawTextChanged: (String) -> Unit,
    onPlatformSelected: (String) -> Unit,
    onInsertTag: (String) -> Unit,
    onApplyCleanSuggestion: () -> Unit,
    onRunGeminiAudit: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onLoadTask: (TranscriptionEntity) -> Unit,
    onLoadSampleBatch: () -> Unit,
    onDeleteTask: (Long) -> Unit,
    onClearQueue: () -> Unit,
    onImportBatch: (text: String, platform: String) -> Unit,
    onOpenHelp: () -> Unit = {}
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var showQueueDrawer by remember { mutableStateOf(false) }

    if (showImportDialog) {
        BatchImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = onImportBatch
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Header & Platform Switcher ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Transcription EvalDesk",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Speech QA & Guideline Formatting",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Help / Guide Button
                    Surface(
                        shape = CircleShape,
                        color = Slate800,
                        modifier = Modifier
                            .clickable { onOpenHelp() }
                            .testTag("open_help_guide_button")
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }

                    // Batch Queue Toggle Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (showQueueDrawer) ElectricCyan else Slate800,
                        modifier = Modifier
                            .clickable { showQueueDrawer = !showQueueDrawer }
                            .testTag("toggle_queue_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Queue (${savedTasks.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (showQueueDrawer) CyanDark else ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Platform Selector & Queue Controls ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val platforms = listOf("Maven", "CrowdGen", "Poliwag", "Custom")
                platforms.forEach { platform ->
                    FilterChip(
                        selected = state.platform == platform,
                        onClick = { onPlatformSelected(platform) },
                        label = { Text(platform, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = CyanDark,
                            containerColor = Slate900,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = state.platform == platform,
                            borderColor = Slate700,
                            selectedBorderColor = ElectricCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = { showImportDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.testTag("open_import_dialog_button")
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = "Import", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import Batch", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onLoadSampleBatch,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.testTag("load_sample_batch_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Samples", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Practice Samples", fontSize = 11.sp)
                }
            }
        }

        // --- 3. Collapsible Task Queue Drawer ---
        if (showQueueDrawer) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Evaluation Task Queue",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (savedTasks.isNotEmpty()) {
                                Text(
                                    "Clear All",
                                    fontSize = 11.sp,
                                    color = CrimsonAlert,
                                    modifier = Modifier.clickable { onClearQueue() }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (savedTasks.isEmpty()) {
                            Text(
                                "Queue is currently empty. Tap 'Samples' or 'Import Batch' to load tasks.",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                savedTasks.take(5).forEach { task ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (state.selectedRecordId == task.id) Slate800 else Color.Transparent)
                                            .clickable { onLoadTask(task) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    task.platform,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ElectricCyan
                                                )
                                                Text(
                                                    "• ${task.status}",
                                                    fontSize = 10.sp,
                                                    color = when (task.status) {
                                                        "APPROVED" -> EmeraldPass
                                                        "REJECTED" -> CrimsonAlert
                                                        else -> AmberWarning
                                                    }
                                                )
                                            }
                                            Text(
                                                task.rawText,
                                                maxLines = 1,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteTask(task.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Slate400,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Quick Audio Tag Insertion Chips ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Insert Audio Guideline Tag:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickTags = listOf(
                        "laughter",
                        "applause",
                        "cough",
                        "music",
                        "background-noise",
                        "inaudible",
                        "screaming",
                        "throat-clearing"
                    )
                    quickTags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate800,
                            modifier = Modifier
                                .clickable { onInsertTag(tag) }
                                .testTag("tag_chip_$tag")
                        ) {
                            Text(
                                "[$tag]",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SkyBlue
                            )
                        }
                    }

                    // Hindi Punctuation quick inserts
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate800,
                        modifier = Modifier.clickable { onInsertTag("।").also { onRawTextChanged(state.rawText + "।") } }
                    ) {
                        Text(
                            "+ danda (।)",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPass
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate800,
                        modifier = Modifier.clickable { onRawTextChanged(state.rawText + " ?") }
                    ) {
                        Text(
                            "+ ?",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPass
                        )
                    }
                }
            }
        }

        // --- 5. Raw Hindi Transcription Input ---
        item {
            ElevatedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Raw Hindi Transcription",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        // Health / Accuracy Badge
                        val score = state.localAnalysis?.accuracyScore ?: 100
                        val scoreColor = when {
                            score >= 90 -> EmeraldPass
                            score >= 70 -> AmberWarning
                            else -> CrimsonAlert
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = scoreColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "QA Score: $score%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.rawText,
                        onValueChange = onRawTextChanged,
                        placeholder = {
                            Text(
                                "Paste or type Hindi audio transcription here to evaluate speech tags, punctuation, and guideline compliance...",
                                color = Slate400,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("hindi_raw_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text statistics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${state.localAnalysis?.wordCount ?: 0} words • ${state.localAnalysis?.characterCount ?: 0} chars",
                            fontSize = 11.sp,
                            color = Slate400
                        )

                        if (!state.localAnalysis?.detectedTags.isNullOrEmpty()) {
                            Text(
                                "Tags: ${state.localAnalysis?.detectedTags?.joinToString { "[$it]" }}",
                                fontSize = 11.sp,
                                color = SkyBlue
                            )
                        }
                    }
                }
            }
        }

        // --- 6. Primary Action Buttons ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gemini Deep AI Audit Button
                Button(
                    onClick = onRunGeminiAudit,
                    enabled = !state.isGeminiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = CyanDark
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("gemini_audit_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (state.isGeminiLoading) {
                        CircularProgressIndicator(
                            color = CyanDark,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auditing...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Audit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Deep AI Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Apply Clean Text Button
                OutlinedButton(
                    onClick = onApplyCleanSuggestion,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("apply_clean_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Auto Clean", fontSize = 12.sp)
                }

                // Approve Button
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPass, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("approve_task_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(16.dp))
                }

                // Reject Button
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("reject_task_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp))
                }
            }
        }

        // Status or Error Toast Banner
        if (state.statusMessage != null || state.errorMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (state.errorMessage != null) CrimsonAlert.copy(alpha = 0.2f) else EmeraldPass.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.errorMessage != null) CrimsonAlert else EmeraldPass
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.errorMessage ?: state.statusMessage.orEmpty(),
                            fontSize = 12.sp,
                            color = if (state.errorMessage != null) CrimsonAlert else EmeraldPass
                        )
                    }
                }
            }
        }

        // --- 7. Clean Formatted Suggestion Panel ---
        if (state.cleanText.isNotBlank() && state.cleanText != state.rawText) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPass.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Sanitized Guideline Format (Ready for submission)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPass
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Clean Hindi", state.cleanText))
                                    Toast.makeText(context, "Clean text copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = EmeraldPass,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            state.cleanText,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        )
                    }
                }
            }
        }

        // --- 8. Gemini Deep AI Analysis Output ---
        if (state.geminiAnalysis != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Text(
                                "Gemini AI Linguistic & QA Evaluation",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            state.geminiAnalysis,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Slate200,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        )
                    }
                }
            }
        }

        // --- 9. Local Rule-Based Linter Issues ---
        item {
            val issues = state.localAnalysis?.issues.orEmpty()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Heuristic Linter Findings (${issues.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (issues.isEmpty() && state.rawText.isNotBlank()) {
                        Text(
                            "All Guidelines Satisfied ✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPass
                        )
                    }
                }

                if (issues.isEmpty() && state.rawText.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPass.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPass.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "No guideline violations detected. Danda punctuation, square tags, and spacing match ${state.platform} standards.",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = EmeraldPass
                        )
                    }
                }

                issues.forEach { issue ->
                    val color = when (issue.severity) {
                        Severity.CRITICAL -> CrimsonAlert
                        Severity.WARNING -> AmberWarning
                        Severity.INFO -> SkyBlue
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = color.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        issue.severity.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    issue.type.name,
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                issue.message,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Fix: ${issue.suggestion}",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }
    }
}
