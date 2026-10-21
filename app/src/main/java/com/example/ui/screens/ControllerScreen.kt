package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConnectionConfigCard
import com.example.ui.components.ControlButtonsCard
import com.example.ui.components.ErrorAlertBanner
import com.example.ui.components.LedHeroDisplay
import com.example.ui.theme.BorderColor
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy850
import com.example.ui.theme.Navy900
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NodeMcuUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ControllerScreen(
    uiState: NodeMcuUiState,
    onTurnOn: () -> Unit,
    onTurnOff: () -> Unit,
    onRefresh: () -> Unit,
    onUpdateUrl: (String) -> Unit,
    onSelectPreset: (String) -> Unit,
    onRetry: () -> Unit,
    onToggleAutoPoll: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection & Hostname Configuration Card
        ConnectionConfigCard(
            baseUrl = uiState.baseUrl,
            connectionStatus = uiState.connectionStatus,
            isPending = uiState.isPending,
            autoPoll = uiState.autoPoll,
            recentUrls = uiState.recentUrls,
            onUpdateUrl = onUpdateUrl,
            onSelectPreset = onSelectPreset,
            onRetry = onRetry,
            onToggleAutoPoll = onToggleAutoPoll
        )

        // Error Banner if network call failed
        AnimatedVisibility(visible = uiState.lastErrorMessage != null) {
            ErrorAlertBanner(
                errorMessage = uiState.lastErrorMessage ?: "",
                errorDetail = uiState.lastErrorDetail,
                onRetry = onRetry,
                onDismiss = onDismissError
            )
        }

        // Hero Glowing LED Display
        LedHeroDisplay(
            ledState = uiState.ledState,
            lastPingMs = uiState.lastPingMs,
            lastUpdated = uiState.lastSuccessfulUpdate,
            rssi = uiState.rssi
        )

        // Hardware Controls (Large ON and OFF Buttons)
        ControlButtonsCard(
            ledState = uiState.ledState,
            isPending = uiState.isPending,
            pendingActionText = uiState.pendingActionText,
            onTurnOn = onTurnOn,
            onTurnOff = onTurnOff,
            onRefresh = onRefresh
        )

        // Last Device Response & State Summary
        if (uiState.lastRawResponse != null) {
            DeviceResponseSummaryCard(
                lastRawResponse = uiState.lastRawResponse,
                lastUpdated = uiState.lastSuccessfulUpdate,
                uptimeSeconds = uiState.uptimeSeconds,
                freeHeapBytes = uiState.freeHeapBytes,
                deviceIp = uiState.deviceIp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DeviceResponseSummaryCard(
    lastRawResponse: String,
    lastUpdated: Long?,
    uptimeSeconds: Long?,
    freeHeapBytes: Long?,
    deviceIp: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Navy900)
            .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("device_response_summary_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Device Response",
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LAST RECEIVED DEVICE RESPONSE",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = NeonGreen
                    )
                }

                if (lastUpdated != null) {
                    Text(
                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastUpdated)),
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Raw response payload block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Navy850)
                    .border(1.dp, Navy800, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = lastRawResponse,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = EmeraldGreen,
                    lineHeight = 16.sp
                )
            }

            // Quick Telemetry Pills
            if (uptimeSeconds != null || freeHeapBytes != null || deviceIp != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uptimeSeconds != null) {
                        val hours = uptimeSeconds / 3600
                        val mins = (uptimeSeconds % 3600) / 60
                        val secs = uptimeSeconds % 60
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy800)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Uptime",
                                tint = NeonCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${hours}h ${mins}m ${secs}s",
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    if (freeHeapBytes != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy800)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "Heap",
                                tint = NeonCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Heap: ${freeHeapBytes / 1024} KB",
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
