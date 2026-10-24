package com.example.ui.keypad

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkKeypad
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosLightKeypad
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadScreen(
    viewModel: KeypadViewModel,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    }

    val primaryTextColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryTextColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val keypadBgColor = if (isDarkTheme) IosDarkKeypad else IosLightKeypad

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- Number Display Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            if (uiState.enteredNumber.isNotEmpty()) {
                                clipboardManager?.setPrimaryClip(
                                    ClipData.newPlainText("Phone Number", uiState.enteredNumber)
                                )
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            } else {
                                val pasteText = clipboardManager?.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!pasteText.isNullOrBlank()) {
                                    viewModel.setNumber(pasteText)
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val textSize = when {
                    uiState.enteredNumber.length > 14 -> 24.sp
                    uiState.enteredNumber.length > 10 -> 30.sp
                    else -> 38.sp
                }

                Text(
                    text = uiState.enteredNumber.ifEmpty { " " },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = textSize,
                        color = primaryTextColor,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    modifier = Modifier.testTag("keypad_display")
                )
            }

            if (uiState.enteredNumber.isNotEmpty()) {
                TextButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                type = ContactsContract.Contacts.CONTENT_TYPE
                                putExtra(ContactsContract.Intents.Insert.PHONE, uiState.enteredNumber)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open contacts", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("add_number_button")
                ) {
                    Text(
                        text = "Add Number",
                        color = IosBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Show subtle paste button if clipboard has numbers
                val clipText = clipboardManager?.primaryClip?.getItemAt(0)?.text?.toString()
                if (!clipText.isNullOrBlank() && clipText.any { it.isDigit() }) {
                    TextButton(
                        onClick = { viewModel.setNumber(clipText) },
                        modifier = Modifier.testTag("paste_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = IosBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paste $clipText",
                                color = IosBlue,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }

        // --- Keypad Buttons Grid (3 x 4) ---
        val keypadRows = listOf(
            listOf(KeypadKey("1", ""), KeypadKey("2", "ABC"), KeypadKey("3", "DEF")),
            listOf(KeypadKey("4", "GHI"), KeypadKey("5", "JKL"), KeypadKey("6", "MNO")),
            listOf(KeypadKey("7", "PQRS"), KeypadKey("8", "TUV"), KeypadKey("9", "WXYZ")),
            listOf(KeypadKey("*", ""), KeypadKey("0", "+"), KeypadKey("#", ""))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            keypadRows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowKeys.forEach { key ->
                        IosKeypadButton(
                            key = key,
                            backgroundColor = keypadBgColor,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor,
                            onClick = { viewModel.onDigitClick(key.digit) },
                            onLongClick = {
                                if (key.digit == "0") {
                                    viewModel.onZeroLongClick()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Bottom Action Row: Spacer | Call Button | Backspace ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left placeholder for symmetry
                Box(modifier = Modifier.size(75.dp))

                // Center Green Call Button
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .clip(CircleShape)
                        .background(IosGreen)
                        .combinedClickable(
                            onClick = { viewModel.initiateCall() }
                        )
                        .testTag("call_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Right Backspace Button
                Box(
                    modifier = Modifier.size(75.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.enteredNumber.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = { viewModel.onBackspaceClick() },
                                    onLongClick = { viewModel.onBackspaceLongClick() }
                                )
                                .testTag("backspace_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Delete",
                                tint = secondaryTextColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Error message notification
        if (uiState.errorMessage != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.dismissError() }) {
                        Text("OK", color = Color.White)
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(uiState.errorMessage ?: "")
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IosKeypadButton(
    key: KeypadKey,
    backgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(75.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("keypad_btn_${key.digit}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = key.digit,
                color = primaryTextColor,
                fontSize = if (key.digit == "*" || key.digit == "#") 36.sp else 32.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 34.sp
            )
            if (key.letters.isNotEmpty()) {
                Text(
                    text = key.letters,
                    color = secondaryTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
