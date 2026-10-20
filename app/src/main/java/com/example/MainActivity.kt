package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CodeVaultScreen
import com.example.ui.screens.EvalDeskScreen
import com.example.ui.theme.CyanDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.WorkstationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WorkstationApp()
            }
        }
    }
}

@Composable
fun WorkstationApp(viewModel: WorkstationViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val evalState by viewModel.evalState.collectAsStateWithLifecycle()
    val vaultState by viewModel.vaultState.collectAsStateWithLifecycle()
    val savedTranscriptions by viewModel.savedTranscriptions.collectAsStateWithLifecycle()
    val savedSnippets by viewModel.savedCodeSnippets.collectAsStateWithLifecycle()
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()

    // Handle back button when in secondary tabs
    if (currentTab != AppTab.EVAL_DESK) {
        BackHandler {
            viewModel.selectTab(AppTab.EVAL_DESK)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                modifier = Modifier.testTag("workstation_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.EVAL_DESK,
                    onClick = { viewModel.selectTab(AppTab.EVAL_DESK) },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "EvalDesk") },
                    label = { Text("EvalDesk", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanDark,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_evaldesk")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CODE_VAULT,
                    onClick = { viewModel.selectTab(AppTab.CODE_VAULT) },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Code Vault") },
                    label = { Text("Code Vault", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanDark,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_codevault")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.selectTab(AppTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanDark,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_analytics")
                )
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.EVAL_DESK -> {
                    EvalDeskScreen(
                        state = evalState,
                        savedTasks = savedTranscriptions,
                        onRawTextChanged = { viewModel.onRawTextChanged(it) },
                        onPlatformSelected = { viewModel.setPlatform(it) },
                        onInsertTag = { viewModel.insertTag(it) },
                        onApplyCleanSuggestion = { viewModel.applyCleanSuggestion() },
                        onRunGeminiAudit = { viewModel.runGeminiTranscriptionAudit() },
                        onApprove = { viewModel.approveCurrentTask() },
                        onReject = { viewModel.rejectCurrentTask() },
                        onLoadTask = { viewModel.loadTranscriptionIntoEditor(it) },
                        onLoadSampleBatch = { viewModel.loadSampleBatchTasks() },
                        onDeleteTask = { viewModel.deleteTranscriptionRecord(it) },
                        onClearQueue = { viewModel.clearAllQueue() },
                        onImportBatch = { text, platform -> viewModel.importBatchData(text, platform) },
                        onOpenHelp = { viewModel.openWelcomeDialog() }
                    )
                }

                AppTab.CODE_VAULT -> {
                    CodeVaultScreen(
                        state = vaultState,
                        savedSnippets = savedSnippets,
                        onCodeChanged = { viewModel.onCodeChanged(it) },
                        onTitleChanged = { viewModel.onCodeTitleChanged(it) },
                        onCategoryChanged = { viewModel.setCodeCategory(it) },
                        onFilterCategoryChanged = { viewModel.setFilterCategory(it) },
                        onRunGeminiCodeAudit = { viewModel.runGeminiCodeAudit() },
                        onSaveSnippet = { viewModel.saveCurrentCodeSnippet() },
                        onLoadSnippet = { viewModel.loadCodeIntoEditor(it) },
                        onDeleteSnippet = { viewModel.deleteCodeSnippet(it) },
                        onLoadSamplePresets = { viewModel.loadSampleCodeSnippets() },
                        onOpenHelp = { viewModel.openWelcomeDialog() }
                    )
                }

                AppTab.DASHBOARD -> {
                    AnalyticsScreen(
                        dailyStats = dailyStats,
                        transcriptions = savedTranscriptions,
                        codeSnippetCount = savedSnippets.size
                    )
                }
            }
        }
    }
}
