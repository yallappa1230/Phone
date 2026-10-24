package com.example.ui.voicemail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CarrierVoicemailConfig
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkKeypad
import com.example.ui.theme.IosDarkSeparator
import com.example.ui.theme.IosDarkSurface
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosLightKeypad
import com.example.ui.theme.IosLightSeparator
import com.example.ui.theme.IosLightSurface
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary

@Composable
fun CarrierVoicemailTab(
    uiState: VoicemailUiState,
    onSimSlotSelected: (Int) -> Unit,
    onCallVoicemail: () -> Unit,
    onUpdateNumber: (Int, String) -> Unit,
    onApplyPreset: (CarrierVoicemailConfig, Int) -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val primaryTextColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryTextColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val surfaceColor = if (isDarkTheme) IosDarkSurface else IosLightSurface
    val separatorColor = if (isDarkTheme) IosDarkSeparator else IosLightSeparator
    val chipBg = if (isDarkTheme) IosDarkKeypad else IosLightKeypad

    var showEditDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val currentSlot = uiState.selectedSimSlot
    val currentNumber = if (currentSlot == 0) uiState.sim1Number else uiState.sim2Number
    val currentCarrierName = if (currentSlot == 0) uiState.sim1CarrierName else uiState.sim2CarrierName

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // SIM Switcher (if dual SIM detected or for configuration)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(chipBg)
                .padding(3.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val sim1Label = uiState.detectedSims.getOrNull(0)?.carrierName ?: "SIM 1"
            val sim2Label = uiState.detectedSims.getOrNull(1)?.carrierName ?: "SIM 2"

            SimTabButton(
                title = sim1Label,
                isSelected = currentSlot == 0,
                isDarkTheme = isDarkTheme,
                onClick = { onSimSlotSelected(0) },
                modifier = Modifier.testTag("sim_tab_0")
            )
            SimTabButton(
                title = sim2Label,
                isSelected = currentSlot == 1,
                isDarkTheme = isDarkTheme,
                onClick = { onSimSlotSelected(1) },
                modifier = Modifier.testTag("sim_tab_1")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large "Call Voicemail" iOS Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SimCard,
                    contentDescription = "Carrier Voicemail",
                    tint = IosBlue,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentCarrierName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor
                )
                Text(
                    text = "Voicemail Number: $currentNumber",
                    fontSize = 14.sp,
                    color = secondaryTextColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Green iOS Call Voicemail Button
                Button(
                    onClick = onCallVoicemail,
                    colors = ButtonDefaults.buttonColors(containerColor = IosGreen),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .testTag("call_voicemail_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Call Voicemail",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Edit Access Number Button
                TextButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.testTag("edit_voicemail_number_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Number",
                        tint = IosBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Change Access Number",
                        color = IosBlue,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Carrier Presets Section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "CARRIER SHORTCODES & PRESETS",
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
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CarrierVoicemailConfig.PRESETS.forEachIndexed { index, preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApplyPreset(preset, currentSlot) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.carrierName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryTextColor
                                )
                                Text(
                                    text = "Access: ${preset.defaultAccessNumber} • Forwarding: ${preset.activationCode}",
                                    fontSize = 12.sp,
                                    color = secondaryTextColor
                                )
                            }
                            if (currentNumber == preset.defaultAccessNumber) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = IosBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (index < CarrierVoicemailConfig.PRESETS.size - 1) {
                            HorizontalDivider(thickness = 0.5.dp, color = separatorColor)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Setup Guide Link
        TextButton(
            onClick = { showHelpDialog = true },
            modifier = Modifier.testTag("voicemail_setup_guide_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = "Help",
                tint = IosBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Jio & Airtel Voicemail Setup Guide",
                color = IosBlue,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Edit Number Dialog
    if (showEditDialog) {
        var tempNumber by remember { mutableStateOf(currentNumber) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Configure Voicemail Number") },
            text = {
                Column {
                    Text(
                        "Enter the carrier voicemail retrieval number for $currentCarrierName:",
                        fontSize = 14.sp,
                        color = secondaryTextColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempNumber,
                        onValueChange = { tempNumber = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_number_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateNumber(currentSlot, tempNumber)
                        showEditDialog = false
                    }
                ) {
                    Text("Save", color = IosBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = secondaryTextColor)
                }
            }
        )
    }

    // Help & Shortcodes Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Carrier Voicemail Instructions") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Reliance Jio:",
                        fontWeight = FontWeight.Bold,
                        color = IosBlue
                    )
                    Text(
                        "• Jio handles voicemail via network call forwarding or the JioCall application.\n• To forward unanswered calls to your mailbox, dial: *401*<voicemail_number>.\n• To cancel all forwarding on Jio, dial: *402.",
                        fontSize = 13.sp,
                        color = primaryTextColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "Bharti Airtel:",
                        fontWeight = FontWeight.Bold,
                        color = IosBlue
                    )
                    Text(
                        "• Airtel Voicemail service operates on 52121.\n• Activate no-reply forwarding: *61*52121#\n• Cancel forwarding: ##61#\n• Dial 52121 to record your personal greeting and listen to saved voice messages.",
                        fontSize = 13.sp,
                        color = primaryTextColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "Note on Cellular Architecture:",
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                    Text(
                        "Carrier voicemail is managed on telecom switches. When you cannot answer, your cellular provider records the caller's message on their secure server.",
                        fontSize = 12.sp,
                        color = secondaryTextColor
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got It", color = IosBlue)
                }
            }
        )
    }
}

@Composable
private fun SimTabButton(
    title: String,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBg = if (isDarkTheme) Color(0xFF636366) else Color.White
    val activeText = if (isDarkTheme) Color.White else Color.Black
    val inactiveText = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeText else inactiveText
        )
    }
}
