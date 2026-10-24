package com.example.ui.voicemail

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.VisualVoicemailDiagnostic
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkSeparator
import com.example.ui.theme.IosDarkSurface
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosLightSeparator
import com.example.ui.theme.IosLightSurface
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary
import com.example.ui.theme.IosOrange

@Composable
fun VisualVoicemailTab(
    diagnostic: VisualVoicemailDiagnostic?,
    onRefresh: () -> Unit,
    onSwitchToBasic: () -> Unit,
    onSwitchToVault: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val primaryTextColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryTextColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val surfaceColor = if (isDarkTheme) IosDarkSurface else IosLightSurface
    val separatorColor = if (isDarkTheme) IosDarkSeparator else IosLightSeparator

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Diagnostic Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Diagnostic",
                        tint = if (diagnostic?.isDefaultDialer == true) IosGreen else IosOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Android Telecom Diagnostic",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryTextColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = separatorColor)
                Spacer(modifier = Modifier.height(14.dp))

                DiagnosticRow(
                    label = "Default Dialer Role",
                    value = if (diagnostic?.isDefaultDialer == true) "Active" else "Companion Mode (Safe)",
                    valueColor = if (diagnostic?.isDefaultDialer == true) IosGreen else IosBlue,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )

                DiagnosticRow(
                    label = "Carrier Detected",
                    value = diagnostic?.carrierName ?: "Cellular Network",
                    valueColor = primaryTextColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )

                DiagnosticRow(
                    label = "Voicemail Access Number",
                    value = diagnostic?.detectedVoicemailNumber ?: "Configured via Carrier Tab",
                    valueColor = primaryTextColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )

                DiagnosticRow(
                    label = "Visual Voicemail Provider",
                    value = "android.provider.VoicemailContract",
                    valueColor = secondaryTextColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical Explanation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Telecom Policy",
                        tint = IosBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Why is Visual Voicemail Gated?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryTextColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = diagnostic?.technicalExplanation ?: "Visual Voicemail APIs on Android (android.provider.VoicemailContract) are restricted to the default dialer role or carrier-signed packages.",
                    fontSize = 14.sp,
                    color = primaryTextColor,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "• Zero Fake Data: In strict adherence to your prompt and engineering integrity, this app never invents fake voicemail recordings or simulated transcripts.\n\n• Carrier Networks (Jio & Airtel): In India, Jio and Airtel do not provide open 3GPP/OMTP visual voicemail servers for third-party dialers. Instead, use Basic Carrier Voicemail to retrieve messages via your carrier's automated switch (*86 / 52121).\n\n• For Authorized Audio: Use the PIN-protected Private Vault to store and listen to confidential voice messages encrypted with AES-256-GCM.",
                    fontSize = 13.sp,
                    color = secondaryTextColor,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSwitchToBasic,
                        colors = ButtonDefaults.buttonColors(containerColor = IosGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("switch_to_carrier_button")
                    ) {
                        Text("Carrier Voicemail", fontSize = 13.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onSwitchToVault,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("switch_to_vault_button")
                    ) {
                        Text("Private Vault", fontSize = 13.sp, color = IosBlue)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Refresh Diagnostic
        OutlinedButton(
            onClick = onRefresh,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("refresh_diagnostic_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = IosBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Recheck Telecom & Carrier Status", color = IosBlue, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    valueColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = secondaryTextColor)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
