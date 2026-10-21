package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HttpLogItem
import com.example.ui.theme.BorderColor
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy850
import com.example.ui.theme.Navy900
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OffRed
import com.example.ui.theme.OffRedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.NodeMcuUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsScreen(
    uiState: NodeMcuUiState,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Live Device Telemetry
        item {
            Text(
                text = "DEVICE TELEMETRY",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = JetBrainsMono,
                color = NeonGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryCard(
                    title = "Wi-Fi Signal",
                    value = if (uiState.rssi != null) "${uiState.rssi} dBm" else "--",
                    subtitle = if (uiState.rssi != null && uiState.rssi > -67) "Strong" else "Moderate",
                    icon = Icons.Default.Wifi,
                    tint = NeonGreen,
                    modifier = Modifier.weight(1f)
                )

                TelemetryCard(
                    title = "Latency (Ping)",
                    value = if (uiState.lastPingMs != null) "${uiState.lastPingMs} ms" else "--",
                    subtitle = "Roundtrip",
                    icon = Icons.Default.Speed,
                    tint = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryCard(
                    title = "ESP8266 Free Heap",
                    value = if (uiState.freeHeapBytes != null) "${uiState.freeHeapBytes / 1024} KB" else "--",
                    subtitle = "Available RAM",
                    icon = Icons.Default.Memory,
                    tint = NeonCyan,
                    modifier = Modifier.weight(1f)
                )

                TelemetryCard(
                    title = "Device IP",
                    value = uiState.deviceIp ?: "--",
                    subtitle = "Local Subnet",
                    icon = Icons.Default.Router,
                    tint = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section: Network & Security Architecture Notes
        item {
            NetworkSecurityGuideCard()
        }

        // Section: HTTP Log Inspector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HTTP TRANSACTION INSPECTOR (${uiState.logs.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = JetBrainsMono,
                    color = NeonGreen,
                    letterSpacing = 1.sp
                )

                if (uiState.logs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearLogs,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 11.sp, fontFamily = JetBrainsMono)
                    }
                }
            }
        }

        if (uiState.logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Navy900)
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "No logs",
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No HTTP transactions logged yet.",
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Tap 'Connect / Test' or toggle the LED to view raw traffic.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(uiState.logs, key = { it.id }) { logItem ->
                LogItemCard(
                    log = logItem,
                    onCopy = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Navy900)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMono,
                    color = TextMuted
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMono,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontFamily = JetBrainsMono,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun LogItemCard(
    log: HttpLogItem,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Navy900)
            .border(
                1.dp,
                if (log.isSuccess) BorderColor else OffRed.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
            .testTag("log_item_${log.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Method & Endpoint
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (log.method == "POST") NeonCyan.copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.method,
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (log.method == "POST") NeonCyan else NeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = log.endpoint,
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                // Status Badge & Latency
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.durationMs != null) {
                        Text(
                            text = "${log.durationMs}ms",
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (log.isSuccess) EmeraldGreen.copy(alpha = 0.2f) else OffRedDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (log.statusCode != null) "${log.statusCode}" else if (log.isSuccess) "OK" else "ERR",
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (log.isSuccess) NeonGreen else OffRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Timestamp
            Text(
                text = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp)),
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                color = TextMuted
            )

            // Body or Error Message
            if (!log.responseBody.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Navy850)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = log.responseBody,
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            color = if (log.isSuccess) EmeraldGreen else TextSecondary,
                            lineHeight = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onCopy(log.responseBody) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            } else if (!log.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = log.errorMessage,
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    color = OffRed,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun NetworkSecurityGuideCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Navy900)
            .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security & Network",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NETWORK & SECURITY ARCHITECTURE",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SecurityTipItem(
                    title = "Local Trusted Network Only",
                    description = "HTTP communication is unencrypted and should only be used on your private, password-protected home or lab Wi-Fi. Do not port-forward port 80 to the public internet.",
                    icon = Icons.Default.LockOpen
                )
                SecurityTipItem(
                    title = "Native Android vs Browser HTTPS Restrictions",
                    description = "Web browsers enforce strict Mixed-Content security policies that block HTTPS pages from contacting plain HTTP devices on a local LAN. This native Android app connects directly over standard TCP socket HTTP without browser mixed-content blocks.",
                    icon = Icons.Default.Router
                )
                SecurityTipItem(
                    title = "mDNS (.local) Resolution",
                    description = "Automatic discovery using 'nodemcu-led.local' requires router and Android Multicast DNS support. If your router isolates AP clients or drops UDP port 5353, use the direct local IP instead.",
                    icon = Icons.Default.Dns
                )
            }
        }
    }
}

@Composable
fun SecurityTipItem(
    title: String,
    description: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonGreen,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = TextSecondary
            )
        }
    }
}
