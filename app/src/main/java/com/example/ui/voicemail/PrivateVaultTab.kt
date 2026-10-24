package com.example.ui.voicemail

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VaultItemEntity
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkBackground
import com.example.ui.theme.IosDarkKeypad
import com.example.ui.theme.IosDarkSeparator
import com.example.ui.theme.IosDarkSurface
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosLightBackground
import com.example.ui.theme.IosLightKeypad
import com.example.ui.theme.IosLightSeparator
import com.example.ui.theme.IosLightSurface
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrivateVaultTab(
    uiState: VoicemailUiState,
    vaultItems: List<VaultItemEntity>,
    onSetupPin: (String) -> Unit,
    onUnlock: (String, (Boolean, String?) -> Unit) -> Unit,
    onLockVault: () -> Unit,
    onPlayItem: (VaultItemEntity) -> Unit,
    onDeleteItem: (VaultItemEntity) -> Unit,
    onAddSampleMessage: (String, String, String, String) -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val primaryTextColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryTextColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val surfaceColor = if (isDarkTheme) IosDarkSurface else IosLightSurface
    val separatorColor = if (isDarkTheme) IosDarkSeparator else IosLightSeparator
    val context = LocalContext.current

    when (uiState.vaultLockState) {
        VaultLockState.NO_PIN_CONFIGURED -> {
            VaultSetupPinView(
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                surfaceColor = surfaceColor,
                onComplete = onSetupPin,
                isDarkTheme = isDarkTheme
            )
        }
        VaultLockState.LOCKED -> {
            VaultPasscodeUnlockView(
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                onUnlock = onUnlock,
                isDarkTheme = isDarkTheme
            )
        }
        VaultLockState.LOCKED_OUT -> {
            VaultLockoutView(
                secondsRemaining = uiState.lockoutSecondsRemaining,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor
            )
        }
        VaultLockState.UNLOCKED -> {
            VaultUnlockedView(
                uiState = uiState,
                vaultItems = vaultItems,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                surfaceColor = surfaceColor,
                separatorColor = separatorColor,
                onLockVault = onLockVault,
                onPlayItem = onPlayItem,
                onDeleteItem = onDeleteItem,
                onAddSampleMessage = onAddSampleMessage,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

// --- Setup PIN View (PIN + Confirm PIN) ---

@Composable
private fun VaultSetupPinView(
    primaryTextColor: Color,
    secondaryTextColor: Color,
    surfaceColor: Color,
    onComplete: (String) -> Unit,
    isDarkTheme: Boolean
) {
    var step by remember { mutableStateOf(1) } // 1: Enter PIN, 2: Confirm PIN
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val currentEntered = if (step == 1) firstPin else confirmPin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Vault Security",
                tint = IosBlue,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (step == 1) "Create Vault PIN" else "Confirm Your PIN",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (step == 1)
                    "Enter a 4-digit PIN to secure your private voicemail messages. Encrypted with hardware-backed Keystore AES-256."
                else
                    "Re-enter your 4-digit PIN to verify.",
                fontSize = 14.sp,
                color = secondaryTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Passcode Dots
            PasscodeDotsRow(
                length = 4,
                filledCount = currentEntered.length,
                isDarkTheme = isDarkTheme
            )

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMsg ?: "",
                    color = IosRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Numeric Keypad
        PasscodeKeypad(
            onDigitClick = { digit ->
                errorMsg = null
                if (step == 1) {
                    if (firstPin.length < 4) {
                        firstPin += digit
                        if (firstPin.length == 4) {
                            step = 2
                        }
                    }
                } else {
                    if (confirmPin.length < 4) {
                        confirmPin += digit
                        if (confirmPin.length == 4) {
                            if (confirmPin == firstPin) {
                                onComplete(firstPin)
                            } else {
                                errorMsg = "PINs did not match. Please try again."
                                step = 1
                                firstPin = ""
                                confirmPin = ""
                            }
                        }
                    }
                }
            },
            onBackspaceClick = {
                errorMsg = null
                if (step == 1 && firstPin.isNotEmpty()) {
                    firstPin = firstPin.dropLast(1)
                } else if (step == 2 && confirmPin.isNotEmpty()) {
                    confirmPin = confirmPin.dropLast(1)
                }
            },
            isDarkTheme = isDarkTheme
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// --- Passcode Unlock View ---

@Composable
private fun VaultPasscodeUnlockView(
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onUnlock: (String, (Boolean, String?) -> Unit) -> Unit,
    isDarkTheme: Boolean
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = IosBlue,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Enter Vault Passcode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Audio files in this vault are encrypted locally.",
                fontSize = 14.sp,
                color = secondaryTextColor
            )

            Spacer(modifier = Modifier.height(28.dp))

            PasscodeDotsRow(
                length = 4,
                filledCount = enteredPin.length,
                isDarkTheme = isDarkTheme
            )

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMsg ?: "",
                    color = IosRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        PasscodeKeypad(
            onDigitClick = { digit ->
                errorMsg = null
                if (enteredPin.length < 4) {
                    val next = enteredPin + digit
                    enteredPin = next
                    if (next.length == 4) {
                        onUnlock(next) { success, err ->
                            if (!success) {
                                errorMsg = err ?: "Incorrect PIN"
                                enteredPin = ""
                            }
                        }
                    }
                }
            },
            onBackspaceClick = {
                errorMsg = null
                if (enteredPin.isNotEmpty()) {
                    enteredPin = enteredPin.dropLast(1)
                }
            },
            isDarkTheme = isDarkTheme
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// --- Lockout View ---

@Composable
private fun VaultLockoutView(
    secondsRemaining: Int,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked Out",
                tint = IosRed,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Vault Locked",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = IosRed
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Too many incorrect PIN attempts. Rate-limiting is active to prevent brute-force attacks.",
                fontSize = 14.sp,
                color = secondaryTextColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Try again in $secondsRemaining seconds",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryTextColor
            )
        }
    }
}

// --- Unlocked Vault View ---

@Composable
private fun VaultUnlockedView(
    uiState: VoicemailUiState,
    vaultItems: List<VaultItemEntity>,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    surfaceColor: Color,
    separatorColor: Color,
    onLockVault: () -> Unit,
    onPlayItem: (VaultItemEntity) -> Unit,
    onDeleteItem: (VaultItemEntity) -> Unit,
    onAddSampleMessage: (String, String, String, String) -> Unit,
    isDarkTheme: Boolean
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = "Unlocked",
                    tint = IosGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Private Vault (AES-GCM)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor
                )
            }

            Row {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("vault_add_message_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Message",
                        tint = IosBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = onLockVault,
                    modifier = Modifier.testTag("vault_lock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Vault",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (vaultItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Empty Vault",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Vault Is Empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Store confidential voice memos or authorized voicemail files. Files are encrypted with AES-GCM-256 backed by Android Keystore.",
                        fontSize = 14.sp,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Voice Note to Vault", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("vault_items_list")
            ) {
                items(vaultItems, key = { it.id }) { item ->
                    VaultItemRow(
                        item = item,
                        isPlaying = uiState.currentlyPlayingItemId == item.id && uiState.isPlaying,
                        playbackProgress = if (uiState.currentlyPlayingItemId == item.id && uiState.playbackDurationMs > 0) {
                            uiState.playbackProgressMs.toFloat() / uiState.playbackDurationMs.toFloat()
                        } else 0f,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        surfaceColor = surfaceColor,
                        separatorColor = separatorColor,
                        onPlayClick = { onPlayItem(item) },
                        onDeleteClick = { onDeleteItem(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("Confidential Voicemail") }
        var senderName by remember { mutableStateOf("Client Memo") }
        var senderNumber by remember { mutableStateOf("+1 555-0199") }
        var notes by remember { mutableStateOf("Review quarterly telecom audit numbers.") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Import Encrypted Voice Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Add a confidential voice recording to your Keystore-encrypted vault:",
                        fontSize = 13.sp,
                        color = secondaryTextColor
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_vault_title_input")
                    )
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = { Text("Sender Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_vault_sender_input")
                    )
                    OutlinedTextField(
                        value = senderNumber,
                        onValueChange = { senderNumber = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_vault_number_input")
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Memo") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddSampleMessage(title, senderName, senderNumber, notes)
                        showAddDialog = false
                    }
                ) {
                    Text("Encrypt & Save", color = IosBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = secondaryTextColor)
                }
            }
        )
    }
}

@Composable
private fun VaultItemRow(
    item: VaultItemEntity,
    isPlaying: Boolean,
    playbackProgress: Float,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    surfaceColor: Color,
    separatorColor: Color,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("vault_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryTextColor
                    )
                    Text(
                        text = "${item.senderName} • ${item.senderNumber}",
                        fontSize = 13.sp,
                        color = secondaryTextColor
                    )
                }

                // Play / Pause Circle Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IosBlue)
                        .clickable(onClick = onPlayClick)
                        .testTag("play_button_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback progress indicator
            LinearProgressIndicator(
                progress = { playbackProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = IosBlue,
                trackColor = separatorColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (item.notes.isNotBlank()) {
                Text(
                    text = "\"${item.notes}\"",
                    fontSize = 13.sp,
                    color = primaryTextColor,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                Text(
                    text = "${dateFormat.format(Date(item.timestamp))} • ${item.durationSeconds}s",
                    fontSize = 12.sp,
                    color = secondaryTextColor
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp).testTag("delete_vault_item_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Secure Delete",
                        tint = IosRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// --- Passcode Dots and Keypad Components ---

@Composable
private fun PasscodeDotsRow(
    length: Int = 4,
    filledCount: Int,
    isDarkTheme: Boolean
) {
    val activeColor = if (isDarkTheme) Color.White else Color.Black
    val inactiveColor = if (isDarkTheme) Color(0xFF3A3A3C) else Color(0xFFC7C7CC)

    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until length) {
            val isFilled = i < filledCount
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isFilled) activeColor else Color.Transparent)
                    .then(
                        if (!isFilled) Modifier.background(inactiveColor, CircleShape) else Modifier
                    )
            )
        }
    }
}

@Composable
private fun PasscodeKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val keypadBg = if (isDarkTheme) IosDarkKeypad else IosLightKeypad
    val textColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary

    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    when (digit) {
                        "" -> {
                            Box(modifier = Modifier.size(70.dp))
                        }
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onBackspaceClick)
                                    .testTag("passcode_backspace"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete",
                                    tint = textColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(keypadBg)
                                    .clickable { onDigitClick(digit) }
                                    .testTag("passcode_key_$digit"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
