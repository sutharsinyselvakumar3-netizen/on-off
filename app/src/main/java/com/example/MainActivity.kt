package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConnectionStatusBadge
import com.example.ui.screens.ControllerScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.FirmwareScreen
import com.example.ui.theme.BorderColor
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy850
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NodeMcuViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NodeMcuViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Navy950),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Navy800)
                                            .border(1.dp, NeonGreen, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "NodeMCU Icon",
                                            tint = NeonGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    androidx.compose.foundation.layout.Column {
                                        Text(
                                            text = "NodeMCU LED Controller",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "ESP8266 ESP-12E • GPIO2 (D4)",
                                            fontFamily = JetBrainsMono,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.refreshStatus(silently = false) },
                                    enabled = !uiState.isPending,
                                    modifier = Modifier.testTag("top_app_bar_refresh")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Status",
                                        tint = if (uiState.isPending) TextMuted else NeonGreen
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Navy900,
                                titleContentColor = TextPrimary
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Navy900,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .border(1.dp, BorderColor)
                                .testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = uiState.selectedTab == 0,
                                onClick = { viewModel.setSelectedTab(0) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Controller"
                                    )
                                },
                                label = {
                                    Text(
                                        "Controller",
                                        fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Navy950,
                                    selectedTextColor = NeonGreen,
                                    indicatorColor = NeonGreen,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_controller")
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 1,
                                onClick = { viewModel.setSelectedTab(1) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Diagnostics"
                                    )
                                },
                                label = {
                                    Text(
                                        "Diagnostics",
                                        fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Navy950,
                                    selectedTextColor = NeonGreen,
                                    indicatorColor = NeonGreen,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_diagnostics")
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 2,
                                onClick = { viewModel.setSelectedTab(2) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.DeveloperBoard,
                                        contentDescription = "Firmware"
                                    )
                                },
                                label = {
                                    Text(
                                        "Firmware",
                                        fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Navy950,
                                    selectedTextColor = NeonGreen,
                                    indicatorColor = NeonGreen,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_firmware")
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Navy950)
                            .padding(paddingValues),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 680.dp)
                        ) {
                            when (uiState.selectedTab) {
                                0 -> ControllerScreen(
                                    uiState = uiState,
                                    onTurnOn = { viewModel.turnLedOn() },
                                    onTurnOff = { viewModel.turnLedOff() },
                                    onRefresh = { viewModel.refreshStatus(silently = false) },
                                    onUpdateUrl = { viewModel.setBaseUrl(it) },
                                    onSelectPreset = { viewModel.selectPresetUrl(it) },
                                    onRetry = { viewModel.refreshStatus(silently = false) },
                                    onToggleAutoPoll = { viewModel.toggleAutoPoll() },
                                    onDismissError = { viewModel.clearErrorMessage() }
                                )
                                1 -> DiagnosticsScreen(
                                    uiState = uiState,
                                    onClearLogs = { viewModel.clearLogs() }
                                )
                                2 -> FirmwareScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
