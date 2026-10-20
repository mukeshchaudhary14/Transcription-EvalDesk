package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyStatEntity
import com.example.data.local.TranscriptionEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun AnalyticsScreen(
    dailyStats: List<DailyStatEntity>,
    transcriptions: List<TranscriptionEntity>,
    codeSnippetCount: Int
) {
    val context = LocalContext.current

    // Aggregates
    val totalEvaluated = dailyStats.sumOf { it.evaluatedCount } + transcriptions.count { it.status != "PENDING" }
    val totalApproved = dailyStats.sumOf { it.approvedCount } + transcriptions.count { it.status == "APPROVED" }
    val totalRejected = dailyStats.sumOf { it.rejectedCount } + transcriptions.count { it.status == "REJECTED" }
    val totalAuditedSnippets = dailyStats.sumOf { it.snippetsAudited } + codeSnippetCount

    val accuracyRate = if (totalEvaluated > 0) {
        ((totalApproved.toFloat() / totalEvaluated.toFloat()) * 100).toInt()
    } else {
        100
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Header ---
        item {
            Column {
                Text(
                    "Throughput & QA Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Local Workstation Performance Metrics",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
        }

        // --- 2. Key Metrics Grid ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Evaluated
                MetricTile(
                    title = "Evaluated",
                    value = totalEvaluated.toString(),
                    subtitle = "Speech tasks",
                    icon = Icons.Default.RecordVoiceOver,
                    tint = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )

                // Approval Rate %
                MetricTile(
                    title = "Approval QA",
                    value = "$accuracyRate%",
                    subtitle = "$totalApproved passed",
                    icon = Icons.Default.CheckCircle,
                    tint = EmeraldPass,
                    modifier = Modifier.weight(1f)
                )

                // C# Patterns Audited
                MetricTile(
                    title = "C# Audits",
                    value = totalAuditedSnippets.toString(),
                    subtitle = "Vault entries",
                    icon = Icons.Default.Code,
                    tint = SkyBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- 3. QA Accuracy Gauge Card ---
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
                            "Overall Quality Target (≥ 95%)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "$accuracyRate%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (accuracyRate >= 95) EmeraldPass else AmberWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (accuracyRate / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (accuracyRate >= 95) EmeraldPass else AmberWarning,
                        trackColor = Slate800,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = EmeraldPass.copy(alpha = 0.2f)) {
                                Text(
                                    "Approved: $totalApproved",
                                    fontSize = 11.sp,
                                    color = EmeraldPass,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = CrimsonAlert.copy(alpha = 0.2f)) {
                                Text(
                                    "Rejected: $totalRejected",
                                    fontSize = 11.sp,
                                    color = CrimsonAlert,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Export Evaluated Batch JSON ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Export Platform Submission Batch",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Export all approved and reviewed transcriptions formatted for Maven / CrowdGen submission.",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val exportArray = JSONArray()
                            transcriptions.forEach { task ->
                                val obj = JSONObject().apply {
                                    put("id", task.id)
                                    put("platform", task.platform)
                                    put("clean_transcript", task.cleanText)
                                    put("raw_transcript", task.rawText)
                                    put("tags", task.tagsDetected)
                                    put("status", task.status)
                                }
                                exportArray.put(obj)
                            }
                            val jsonString = exportArray.toString(2)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Evaluated Batch JSON", jsonString))
                            Toast.makeText(context, "Exported ${transcriptions.size} tasks to clipboard as JSON!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = CyanDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("export_batch_json_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Batch JSON to Clipboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 5. Error Category Breakdown ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Common Error Distribution",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    ErrorFrequencyRow("Danda (।) vs Period (.) Mismatch", "42% of QA flags", CrimsonAlert)
                    ErrorFrequencyRow("Noise Tag Bracket & Spacing", "28% of QA flags", AmberWarning)
                    ErrorFrequencyRow("Event Delegate Memory Leaks (+=)", "18% of C# flags", CrimsonAlert)
                    ErrorFrequencyRow("Static Collection Concurrency", "12% of C# flags", AmberWarning)
                }
            }
        }
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate400)
            Text(subtitle, fontSize = 10.sp, color = tint)
        }
    }
}

@Composable
fun ErrorFrequencyRow(label: String, count: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
            Text(label, fontSize = 12.sp, color = Slate400)
        }
        Text(count, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
