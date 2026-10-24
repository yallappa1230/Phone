package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkBackground
import com.example.ui.theme.IosDarkSeparator
import com.example.ui.theme.IosDarkSurface
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosLightBackground
import com.example.ui.theme.IosLightSeparator
import com.example.ui.theme.IosLightSurface
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosRed

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val autoLockMinutes by viewModel.autoLockMinutes.collectAsState()
    val context = LocalContext.current

    var showClearVaultDialog by remember { mutableStateOf(false) }

    val primaryTextColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryTextColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val surfaceColor = if (isDarkTheme) IosDarkSurface else IosLightSurface
    val separatorColor = if (isDarkTheme) IosDarkSeparator else IosLightSeparator

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) IosDarkBackground else IosLightBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = IosBlue
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- Theme Section ---
            SettingsGroup(
                title = "APPEARANCE",
                surfaceColor = surfaceColor,
                secondaryTextColor = secondaryTextColor
            ) {
                val themes = listOf(
                    "SYSTEM" to "Automatic (System)",
                    "DARK" to "Dark Mode (iOS Black)",
                    "LIGHT" to "Light Mode"
                )

                themes.forEachIndexed { index, (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setThemeMode(key) }
                            .padding(vertical = 12.dp)
                            .testTag("theme_option_$key"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            color = primaryTextColor
                        )
                        if (themeMode == key) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = IosBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (index < themes.size - 1) {
                        HorizontalDivider(thickness = 0.5.dp, color = separatorColor)
                    }
                }
            }

            // --- Keypad Haptics Section ---
            SettingsGroup(
                title = "KEYPAD & FEEDBACK",
                surfaceColor = surfaceColor,
                secondaryTextColor = secondaryTextColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keypad Vibration",
                            fontSize = 16.sp,
                            color = primaryTextColor
                        )
                        Text(
                            text = "Tactile haptic feedback on digit tap and dialer actions",
                            fontSize = 12.sp,
                            color = secondaryTextColor
                        )
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = { viewModel.setHapticEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IosGreen
                        ),
                        modifier = Modifier.testTag("haptic_switch")
                    )
                }
            }

            // --- Private Vault Settings ---
            SettingsGroup(
                title = "PRIVATE VAULT ENCRYPTION",
                surfaceColor = surfaceColor,
                secondaryTextColor = secondaryTextColor
            ) {
                // Auto-lock timeouts
                Text(
                    text = "Auto-Lock Inactivity Timer:",
                    fontSize = 14.sp,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                val timeouts = listOf(1 to "1 minute", 2 to "2 minutes", 5 to "5 minutes")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    timeouts.forEach { (minutes, label) ->
                        val isSelected = autoLockMinutes == minutes
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) IosBlue else Color.Transparent)
                                .clickable { viewModel.setAutoLockMinutes(minutes) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("timeout_$minutes"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else primaryTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = separatorColor)
                Spacer(modifier = Modifier.height(8.dp))

                // Reset Vault Button
                TextButton(
                    onClick = { showClearVaultDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("reset_vault_button")
                ) {
                    Text(
                        text = "Reset Vault PIN & Clear Recordings",
                        color = IosRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // --- Permissions & Privacy Section ---
            SettingsGroup(
                title = "PRIVACY & ANDROID PERMISSIONS",
                surfaceColor = surfaceColor,
                secondaryTextColor = secondaryTextColor
            ) {
                PermissionStatusRow(
                    label = "Contacts Access",
                    isGranted = uiState.hasContactsPermission,
                    description = "Used to display and search your on-device address book.",
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )
                HorizontalDivider(thickness = 0.5.dp, color = separatorColor)
                PermissionStatusRow(
                    label = "Call Log Access",
                    isGranted = uiState.hasCallLogPermission,
                    description = "Used to view genuine recent calls and missed call history.",
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor
                )
                HorizontalDivider(thickness = 0.5.dp, color = separatorColor)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage App Permissions in Android Settings", color = IosBlue, fontSize = 14.sp)
                }
            }

            // --- About & Architecture Statement ---
            SettingsGroup(
                title = "ABOUT THIS DIALER",
                surfaceColor = surfaceColor,
                secondaryTextColor = secondaryTextColor
            ) {
                Text(
                    text = "iPhone-Style Companion Dialer",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Version 1.0.0 (Native Android & Jetpack Compose)\n\n• Zero Fake Data: Operates with 100% genuine APIs.\n• Companion Mode: Hands off outgoing calls to your system dialer via ACTION_DIAL without taking over the system telephony role.\n• Hardware Keystore: AES-256-GCM authenticated encryption for private voicemail vault storage.",
                    fontSize = 12.sp,
                    color = secondaryTextColor,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        uiState.statusMessage?.let { msg ->
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.clearStatusMessage() }) {
                        Text("OK", color = Color.White)
                    }
                },
                modifier = Modifier.padding(12.dp)
            ) {
                Text(msg)
            }
        }
    }

    if (showClearVaultDialog) {
        AlertDialog(
            onDismissRequest = { showClearVaultDialog = false },
            title = { Text("Reset Private Vault?") },
            text = {
                Text(
                    "This will delete your vault PIN and permanently wipe all encrypted voice recordings stored in the vault. This action cannot be undone.",
                    fontSize = 14.sp,
                    color = secondaryTextColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetVaultPin()
                        showClearVaultDialog = false
                    }
                ) {
                    Text("Delete Everything", color = IosRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearVaultDialog = false }) {
                    Text("Cancel", color = secondaryTextColor)
                }
            }
        )
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    surfaceColor: Color,
    secondaryTextColor: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = secondaryTextColor,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(
    label: String,
    isGranted: Boolean,
    description: String,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 15.sp, color = primaryTextColor, fontWeight = FontWeight.Medium)
            Text(text = description, fontSize = 12.sp, color = secondaryTextColor)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (isGranted) "Granted" else "Not Granted",
                tint = if (isGranted) IosGreen else IosOrange,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isGranted) "Allowed" else "Denied",
                fontSize = 13.sp,
                color = if (isGranted) IosGreen else IosOrange,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
