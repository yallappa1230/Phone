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
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkSurface
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosLightSurface
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary
import com.example.ui.theme.IosOrange

@Composable
fun CallInterceptionInfoDialog(
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val primaryTextColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryTextColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val surfaceColor = if (isDarkTheme) IosDarkSurface else IosLightSurface

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Architecture Report",
                    tint = IosBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Android Telecom & Voicemail Architecture",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
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
                    text = "Technical Breakdown of Automatic Unanswered-Call Answering on Android:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IosOrange
                )

                ArchitectureItem(
                    icon = Icons.Default.PhoneInTalk,
                    iconColor = IosBlue,
                    title = "1. Carrier-Hosted Voicemail",
                    description = "When a call is unanswered, the cellular tower network (e.g., Jio, Airtel, Verizon) redirects the caller to the carrier's automated switchboard. The message is stored in the carrier's data center and retrieved via shortcodes (*86 / 52121). This works regardless of device power or connectivity.",
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )

                ArchitectureItem(
                    icon = Icons.Default.Podcasts,
                    iconColor = IosGreen,
                    title = "2. Android Visual Voicemail",
                    description = "Android provides android.provider.VoicemailContract and VisualVoicemailService. However, Google strictly gates these APIs: only the system Default Dialer or carrier-privileged apps signed with the carrier's private platform key can sync or read messages. Companion dialers using ACTION_DIAL cannot access this private database.",
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )

                ArchitectureItem(
                    icon = Icons.Default.Lock,
                    iconColor = IosOrange,
                    title = "3. Call Interception & Recording Restrictions",
                    description = "Since Android 9 (Pie) and Android 10, Google removed raw in-call audio recording APIs (VOICE_CALL audio source). A standard user-space app without carrier firmware signatures or default telephony roles CANNOT silently auto-answer cellular calls or record incoming audio streams in the background.",
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )

                ArchitectureItem(
                    icon = Icons.Default.Security,
                    iconColor = IosBlue,
                    title = "4. Private Vault Storage (Implemented)",
                    description = "To meet your security needs honestly, this application implements a PIN-protected local vault. Legitimate recordings, voicemails, or audio notes imported by the user are encrypted on-device using Android Keystore-backed AES-256-GCM. Unreadable by other apps and never uploaded to cloud.",
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Understood", color = IosBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ArchitectureItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryTextColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = secondaryTextColor,
            lineHeight = 17.sp
        )
    }
}
