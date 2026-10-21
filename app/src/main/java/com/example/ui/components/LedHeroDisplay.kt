package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LedState
import com.example.ui.theme.BorderColor
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy850
import com.example.ui.theme.Navy900
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OffRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LedHeroDisplay(
    ledState: LedState,
    lastPingMs: Long?,
    lastUpdated: Long?,
    rssi: Int?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LedGlowTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val (glowColor, coreColor, stateTitle, stateSubtitle) = when (ledState) {
        LedState.ON -> Quadruple(
            NeonGreen.copy(alpha = pulseAlpha),
            NeonGreen,
            "LED IS ON",
            "GPIO 2 (D4) Active LOW: 0V Sink"
        )
        LedState.OFF -> Quadruple(
            OffRed.copy(alpha = 0.2f),
            Color(0xFF4A5568),
            "LED IS OFF",
            "GPIO 2 (D4) Inactive: 3.3V High"
        )
        LedState.UNKNOWN -> Quadruple(
            WarningAmber.copy(alpha = 0.25f),
            Color(0xFF64748B),
            "STATUS UNKNOWN",
            "Send query or check NodeMCU power"
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Navy850, Navy900)
                )
            )
            .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
            .padding(24.dp)
            .testTag("led_hero_display"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hardware Tag Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Navy800)
                    .border(1.dp, BorderColor, RoundedCornerShape(100.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ESP-12E ONBOARD BLUE LED",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonGreen,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Glowing LED Ring Graphic
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background radial glow
                Canvas(modifier = Modifier.size(160.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, Color.Transparent),
                            center = center,
                            radius = size.width / 2
                        ),
                        radius = size.width / 2,
                        center = center
                    )
                }

                // Inner bezel ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Navy800)
                        .border(
                            width = 2.dp,
                            color = if (ledState == LedState.ON) NeonGreen else BorderColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // LED Core Bulb
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(
                                elevation = if (ledState == LedState.ON) 18.dp else 2.dp,
                                shape = CircleShape,
                                spotColor = coreColor
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (ledState == LedState.ON) {
                                        listOf(Color.White, NeonGreen, EmeraldGreen)
                                    } else if (ledState == LedState.OFF) {
                                        listOf(Color(0xFF64748B), Color(0xFF334155), Color(0xFF1E293B))
                                    } else {
                                        listOf(WarningAmber, Color(0xFF64748B), Navy800)
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (ledState) {
                                LedState.ON -> Icons.Default.Bolt
                                LedState.OFF -> Icons.Default.PowerOff
                                LedState.UNKNOWN -> Icons.Default.HelpOutline
                            },
                            contentDescription = "LED State Icon",
                            tint = if (ledState == LedState.ON) Navy900 else TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State Title
            Text(
                text = stateTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = when (ledState) {
                    LedState.ON -> NeonGreen
                    LedState.OFF -> TextPrimary
                    LedState.UNKNOWN -> WarningAmber
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = stateSubtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = JetBrainsMono,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Telemetry Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Navy800.copy(alpha = 0.6f))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Latency Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Ping",
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lastPingMs != null) "${lastPingMs}ms" else "-- ms",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }

                // Wi-Fi RSSI Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Signal",
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (rssi != null) "$rssi dBm" else "-- dBm",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }

                // Last Updated
                Text(
                    text = if (lastUpdated != null) {
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastUpdated))
                    } else {
                        "Never"
                    },
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
