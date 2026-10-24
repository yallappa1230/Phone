package com.example.ui.voicemail

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkBackground
import com.example.ui.theme.IosDarkKeypad
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosLightBackground
import com.example.ui.theme.IosLightKeypad
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary

@Composable
fun VoicemailScreen(
    viewModel: VoicemailViewModel,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val vaultItems by viewModel.vaultItems.collectAsState()

    var showArchitectureDialog by remember { mutableStateOf(false) }

    val primaryTextColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryTextColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val segmentBg = if (isDarkTheme) IosDarkKeypad else IosLightKeypad

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) IosDarkBackground else IosLightBackground)
    ) {
        // Top iOS Segmented Control: Carrier | Visual | Vault
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Voicemail",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                )

                IconButton(
                    onClick = { showArchitectureDialog = true },
                    modifier = Modifier.testTag("voicemail_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Architecture Report",
                        tint = IosBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3-way Segmented Control
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(segmentBg)
                    .padding(2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    VoicemailSegmentItem(
                        title = "Carrier",
                        isSelected = uiState.currentSection == VoicemailSection.CARRIER_VOICEMAIL,
                        isDarkTheme = isDarkTheme,
                        onClick = { viewModel.setSection(VoicemailSection.CARRIER_VOICEMAIL) },
                        modifier = Modifier.weight(1f).testTag("voicemail_segment_carrier")
                    )
                    VoicemailSegmentItem(
                        title = "Visual",
                        isSelected = uiState.currentSection == VoicemailSection.VISUAL_VOICEMAIL,
                        isDarkTheme = isDarkTheme,
                        onClick = { viewModel.setSection(VoicemailSection.VISUAL_VOICEMAIL) },
                        modifier = Modifier.weight(1f).testTag("voicemail_segment_visual")
                    )
                    VoicemailSegmentItem(
                        title = "Private Vault",
                        isSelected = uiState.currentSection == VoicemailSection.PRIVATE_VAULT,
                        isDarkTheme = isDarkTheme,
                        onClick = { viewModel.setSection(VoicemailSection.PRIVATE_VAULT) },
                        modifier = Modifier.weight(1f).testTag("voicemail_segment_vault")
                    )
                }
            }
        }

        // Section Content
        Box(modifier = Modifier.weight(1f)) {
            when (uiState.currentSection) {
                VoicemailSection.CARRIER_VOICEMAIL -> {
                    CarrierVoicemailTab(
                        uiState = uiState,
                        onSimSlotSelected = { viewModel.setSelectedSimSlot(it) },
                        onCallVoicemail = { viewModel.callVoicemail() },
                        onUpdateNumber = { slot, num -> viewModel.updateCarrierNumber(slot, num) },
                        onApplyPreset = { preset, slot -> viewModel.applyPreset(preset, slot) },
                        isDarkTheme = isDarkTheme
                    )
                }
                VoicemailSection.VISUAL_VOICEMAIL -> {
                    VisualVoicemailTab(
                        diagnostic = uiState.visualVoicemailDiagnostic,
                        onRefresh = { viewModel.refreshVisualVoicemailDiagnostic() },
                        onSwitchToBasic = { viewModel.setSection(VoicemailSection.CARRIER_VOICEMAIL) },
                        onSwitchToVault = { viewModel.setSection(VoicemailSection.PRIVATE_VAULT) },
                        isDarkTheme = isDarkTheme
                    )
                }
                VoicemailSection.PRIVATE_VAULT -> {
                    PrivateVaultTab(
                        uiState = uiState,
                        vaultItems = vaultItems,
                        onSetupPin = { viewModel.setupPin(it) },
                        onUnlock = { pin, cb -> viewModel.unlockVault(pin, cb) },
                        onLockVault = { viewModel.lockVault() },
                        onPlayItem = { viewModel.playVaultAudio(it) },
                        onDeleteItem = { viewModel.deleteVaultItem(it) },
                        onAddSampleMessage = { title, sender, num, notes ->
                            viewModel.addDemoSecureVoicemail(title, sender, num, notes)
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }

        // Notification Snackbar
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

    if (showArchitectureDialog) {
        CallInterceptionInfoDialog(
            onDismiss = { showArchitectureDialog = false },
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
private fun VoicemailSegmentItem(
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
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) activeBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
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
