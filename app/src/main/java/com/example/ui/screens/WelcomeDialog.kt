package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun WelcomeDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = ElectricCyan.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        "Welcome to EvalDesk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Audio QA & C# Architectural Hub",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "EvalDesk is designed for speech evaluation specialists and C# software engineers to streamline transcription QA and maintain thread-safe code patterns.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate200,
                    lineHeight = 18.sp
                )

                // Feature 1: EvalDesk
                FeatureCard(
                    title = "Hindi Transcription EvalDesk",
                    description = "Validate audio transcripts against Maven, CrowdGen, and Poliwag standards. Instantly detects missing danda (।), bracket noise tags like [laughter] or [applause], spacing violations, and auto-sanitizes with one tap.",
                    icon = Icons.Default.RecordVoiceOver,
                    tint = ElectricCyan
                )

                // Feature 2: Code Vault
                FeatureCard(
                    title = "C# Concurrency & Code Vault",
                    description = "Audit C# code for race conditions, unsynchronized static collections, dangerous lock(this), and event delegate memory leaks (+= without -=). Store verified architectural patterns in your local vault.",
                    icon = Icons.Default.Code,
                    tint = SkyBlue
                )

                // Feature 3: Analytics
                FeatureCard(
                    title = "Throughput & Batch Management",
                    description = "Track daily completed tasks, approval rates, and error distributions. Import JSON/CSV batch files and copy clean, submission-ready datasets with one tap.",
                    icon = Icons.Default.Assessment,
                    tint = EmeraldPass
                )

                // Where to Start Section
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate950,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Where to Start:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        StartStepRow(step = "1", text = "Paste a transcript into EvalDesk or tap 'Practice Samples' to see instant rule checks.")
                        StartStepRow(step = "2", text = "Switch to Code Vault to inspect C# classes or load architectural concurrency templates.")
                        StartStepRow(step = "3", text = "Check Analytics to monitor your throughput and export batches.")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = CyanDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_get_started_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Get Started", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Slate900,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    tint: Color
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    description,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
private fun StartStepRow(step: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = ElectricCyan.copy(alpha = 0.2f),
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    step,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
            }
        }
        Text(
            text,
            fontSize = 11.sp,
            color = Slate200,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
