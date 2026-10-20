package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.Slate800

@Composable
fun BatchImportDialog(
    onDismiss: () -> Unit,
    onImport: (text: String, platform: String) -> Unit
) {
    var importText by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("Maven") }
    val platforms = listOf("Maven", "CrowdGen", "Poliwag", "Custom")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Import Batch Tasks (JSON / CSV / Lines)", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Select Platform Profile:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    platforms.forEach { platform ->
                        FilterChip(
                            selected = selectedPlatform == platform,
                            onClick = { selectedPlatform = platform },
                            label = { Text(platform, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Paste raw batch JSON array, CSV with text column, or one Hindi transcript per line:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    placeholder = {
                        Text(
                            "[\n  {\"raw_text\": \"नमस्ते आज हम...\"}\n]\n\nOR line-by-line Hindi sentences...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("import_text_field"),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (importText.isNotBlank()) {
                        onImport(importText, selectedPlatform)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("confirm_import_button")
            ) {
                Text("Import to Queue")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_import_button")
            ) {
                Text("Cancel")
            }
        },
        containerColor = Slate800,
        shape = RoundedCornerShape(16.dp)
    )
}
