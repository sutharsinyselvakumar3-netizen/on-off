package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.ui.theme.BorderColor
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy850
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OffRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConnectionConfigCard(
    baseUrl: String,
    connectionStatus: ConnectionStatus,
    isPending: Boolean,
    autoPoll: Boolean,
    recentUrls: List<String>,
    onUpdateUrl: (String) -> Unit,
    onSelectPreset: (String) -> Unit,
    onRetry: () -> Unit,
    onToggleAutoPoll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember(baseUrl) { mutableStateOf(baseUrl) }
    var showHelp by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Navy900)
            .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .testTag("connection_config_card")
    ) {
        Column {
            // Header with Connection Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lan,
                        contentDescription = "LAN Target",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NETWORK TARGET",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = JetBrainsMono,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                }

                // Status Badge
                ConnectionStatusBadge(status = connectionStatus)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // URL Input Field
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("NodeMCU Hostname or IP Address") },
                placeholder = { Text("http://nodemcu-led.local") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_url_input"),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    color = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onUpdateUrl(textInput)
                        onRetry()
                    }
                ),
                trailingIcon = {
                    if (textInput != baseUrl) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                onUpdateUrl(textInput)
                                onRetry()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Apply URL",
                                tint = NeonGreen
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Navy850,
                    unfocusedContainerColor = Navy850,
                    focusedLabelColor = NeonGreen,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = NeonGreen
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Chips Row
            Text(
                text = "Quick Presets:",
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentUrls.forEach { url ->
                    val isSelected = url.equals(baseUrl.trimEnd('/'), ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Navy700 else Navy800)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NeonGreen else BorderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                textInput = url
                                onSelectPreset(url)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = url.replace("http://", ""),
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NeonGreen else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Connect / Retry Button + Auto-poll Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onUpdateUrl(textInput)
                        onRetry()
                    },
                    enabled = !isPending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy700,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier.testTag("connect_retry_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Retry",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Connect / Test",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }

                // Auto-poll toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Navy850)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Auto-Poll (5s)",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = autoPoll,
                        onCheckedChange = { onToggleAutoPoll() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = DarkGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = Navy800
                        ),
                        modifier = Modifier.testTag("auto_poll_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Network requirement note toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showHelp = !showHelp }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Network Info",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showHelp) "Hide discovery details" else "How discovery & local Wi-Fi work...",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            AnimatedVisibility(visible = showHelp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Navy850)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "• Both your Android device and the NodeMCU must be connected to the exact same Wi-Fi network.\n" +
                                    "• 'nodemcu-led.local' uses Multicast DNS (mDNS / Bonjour). If your router blocks multicast packets, enter the NodeMCU's assigned local IP address (e.g., 192.168.1.150) shown in the Arduino Serial Monitor.\n" +
                                    "• HTTP communication is designed strictly for local trusted private networks; never expose the NodeMCU server to the public internet without authentication.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusBadge(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val (dotColor, label, textColor) = when (status) {
        ConnectionStatus.CONNECTED -> Triple(NeonGreen, "CONNECTED", NeonGreen)
        ConnectionStatus.CONNECTING -> Triple(WarningAmber, "CONNECTING...", WarningAmber)
        ConnectionStatus.DISCONNECTED -> Triple(TextMuted, "DISCONNECTED", TextMuted)
        ConnectionStatus.ERROR -> Triple(OffRed, "OFFLINE", OffRed)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Navy800)
            .border(1.dp, BorderColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
