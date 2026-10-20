package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CodeSnippetEntity
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
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.CodeVaultUiState

@Composable
fun CodeVaultScreen(
    state: CodeVaultUiState,
    savedSnippets: List<CodeSnippetEntity>,
    onCodeChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onFilterCategoryChanged: (String) -> Unit,
    onRunGeminiCodeAudit: () -> Unit,
    onSaveSnippet: () -> Unit,
    onLoadSnippet: (CodeSnippetEntity) -> Unit,
    onDeleteSnippet: (Long) -> Unit,
    onLoadSamplePresets: () -> Unit,
    onOpenHelp: () -> Unit = {}
) {
    val context = LocalContext.current
    var showSavedDrawer by remember { mutableStateOf(false) }

    val categories = listOf("All", "Concurrency", "Events & Delegates", "Memory & IDisposable", "Async Patterns")

    val filteredSnippets = if (state.filterCategory == "All") {
        savedSnippets
    } else {
        savedSnippets.filter { it.category == state.filterCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Title Header & Vault Toggle ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "C# Code Vault",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Thread Safety & Delegate Leak Auditor",
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
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Slate800,
                        modifier = Modifier
                            .clickable { onOpenHelp() }
                            .testTag("open_vault_help_guide_button")
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

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (showSavedDrawer) ElectricCyan else Slate800,
                        modifier = Modifier
                            .clickable { showSavedDrawer = !showSavedDrawer }
                            .testTag("toggle_vault_drawer_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = "Vault",
                                tint = if (showSavedDrawer) CyanDark else ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Vault (${savedSnippets.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (showSavedDrawer) CyanDark else ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Category Filter & Preset Loader ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = state.filterCategory == category,
                        onClick = { onFilterCategoryChanged(category) },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = CyanDark,
                            containerColor = Slate900,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = state.filterCategory == category,
                            borderColor = Slate700,
                            selectedBorderColor = ElectricCyan
                        )
                    )
                }

                OutlinedButton(
                    onClick = onLoadSamplePresets,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.testTag("load_csharp_presets_button")
                ) {
                    Text("Architecture Presets", fontSize = 11.sp)
                }
            }
        }

        // --- 3. Collapsible Saved Snippets Drawer ---
        if (showSavedDrawer) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Saved Architectural Patterns (${filteredSnippets.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (filteredSnippets.isEmpty()) {
                            Text(
                                "No snippets saved in this category yet. Tap 'Load Presets' to add architectural references.",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                filteredSnippets.forEach { snippet ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (state.selectedSnippetId == snippet.id) Slate800 else Color.Transparent)
                                            .clickable { onLoadSnippet(snippet) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = when {
                                                        snippet.safetyScore >= 80 -> EmeraldPass.copy(alpha = 0.2f)
                                                        snippet.safetyScore >= 50 -> AmberWarning.copy(alpha = 0.2f)
                                                        else -> CrimsonAlert.copy(alpha = 0.2f)
                                                    }
                                                ) {
                                                    Text(
                                                        "${snippet.safetyScore}/100",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when {
                                                            snippet.safetyScore >= 80 -> EmeraldPass
                                                            snippet.safetyScore >= 50 -> AmberWarning
                                                            else -> CrimsonAlert
                                                        },
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Text(
                                                    snippet.category,
                                                    fontSize = 10.sp,
                                                    color = SkyBlue
                                                )
                                            }
                                            Text(
                                                snippet.title,
                                                maxLines = 1,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteSnippet(snippet.id) },
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

        // --- 4. Snippet Title & Category Inputs ---
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
                            "Pattern Descriptor",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        // Safety Score Gauge
                        val score = state.localAnalysis?.score ?: 100
                        val scoreColor = when {
                            score >= 80 -> EmeraldPass
                            score >= 50 -> AmberWarning
                            else -> CrimsonAlert
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = scoreColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = scoreColor, modifier = Modifier.size(12.dp))
                                Text(
                                    "Safety: $score/100",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.title,
                        onValueChange = onTitleChanged,
                        placeholder = { Text("Title (e.g. Thread-Safe Event Aggregator)", color = Slate400, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("csharp_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "C# Source Code (Monospace Inspector):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = state.code,
                        onValueChange = onCodeChanged,
                        placeholder = {
                            Text(
                                "Paste or write C# code here to scan for thread-safety hazards, race conditions, and delegate memory leaks...",
                                color = Slate400,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag("csharp_code_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Detected Concurrency Patterns Chips
                    val detectedPatterns = state.localAnalysis?.detectedPatterns.orEmpty()
                    if (detectedPatterns.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            detectedPatterns.forEach { pattern ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Slate800
                                ) {
                                    Text(
                                        pattern,
                                        fontSize = 10.sp,
                                        color = SkyBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Action Buttons Row ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Deep Gemini AI Concurrency Scan
                Button(
                    onClick = onRunGeminiCodeAudit,
                    enabled = !state.isGeminiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = CyanDark
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("gemini_code_scan_button"),
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
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Safety Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Save to Vault
                OutlinedButton(
                    onClick = onSaveSnippet,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_csharp_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontSize = 12.sp)
                }

                // Copy Code
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("C# Code", state.code))
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_csharp_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
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

        // --- 6. Gemini Deep AI Concurrency Audit Output ---
        if (state.geminiAdvice != null) {
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
                                "Gemini Concurrency & Architectural Audit",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            state.geminiAdvice,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
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

        // --- 7. Local Static Safety Scanner Findings ---
        item {
            val issues = state.localAnalysis?.issues.orEmpty()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Vulnerability Scanner (${issues.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (issues.isEmpty() && state.code.isNotBlank()) {
                        Text(
                            "No Concurrency Pitfalls Found ✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPass
                        )
                    }
                }

                if (issues.isEmpty() && state.code.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPass.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPass.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Clean concurrency profile. No unprotected static collections, lock(this), sync-over-async blocking, or lambda event leak hazards detected.",
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
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
                                    issue.category.name.replace("_", " "),
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                issue.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                issue.description,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate950,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Fix: ${issue.recommendation}",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = EmeraldPass,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
