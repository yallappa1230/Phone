package com.example.ui.recents

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkBackground
import com.example.ui.theme.IosDarkKeypad
import com.example.ui.theme.IosDarkSeparator
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosLightBackground
import com.example.ui.theme.IosLightKeypad
import com.example.ui.theme.IosLightSeparator
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary
import com.example.ui.theme.IosRed

@Composable
fun RecentsScreen(
    viewModel: RecentsViewModel,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val primaryTextColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryTextColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val segmentBg = if (isDarkTheme) IosDarkKeypad else IosLightKeypad
    val separatorColor = if (isDarkTheme) IosDarkSeparator else IosLightSeparator

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) IosDarkBackground else IosLightBackground)
    ) {
        // Top iOS Segmented Bar: All | Missed
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(segmentBg)
                    .padding(2.dp)
            ) {
                Row {
                    IosSegmentTab(
                        title = "All",
                        isSelected = uiState.filter == RecentsFilter.ALL,
                        isDarkTheme = isDarkTheme,
                        onClick = { viewModel.setFilter(RecentsFilter.ALL) },
                        modifier = Modifier.testTag("recents_tab_all")
                    )
                    IosSegmentTab(
                        title = "Missed",
                        isSelected = uiState.filter == RecentsFilter.MISSED,
                        isDarkTheme = isDarkTheme,
                        onClick = { viewModel.setFilter(RecentsFilter.MISSED) },
                        modifier = Modifier.testTag("recents_tab_missed")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Recents",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                )
            }
        }

        if (!uiState.hasPermission) {
            // Honest Fallback Screen (never fabricating fake calls)
            CallLogPermissionDeniedView(
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
                }
            )
        } else if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PhoneCallback,
                        contentDescription = "No Recents",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.filter == RecentsFilter.MISSED) "No Missed Calls" else "No Recent Calls",
                        color = primaryTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Calls made or received through your system dialer will appear here.",
                        color = secondaryTextColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .testTag("recents_list")
            ) {
                items(callLogs, key = { it.id }) { callItem ->
                    RecentsRow(
                        callItem = callItem,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        separatorColor = separatorColor,
                        onCallBack = { viewModel.callBack(callItem.number) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IosSegmentTab(
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
            .padding(horizontal = 28.dp, vertical = 6.dp),
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

@Composable
private fun RecentsRow(
    callItem: CallLogItem,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    separatorColor: Color,
    onCallBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCallBack)
            .testTag("recent_item_${callItem.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Direction icon
            val (icon, iconTint) = getCallTypeIcon(callItem.callType)
            Icon(
                imageVector = icon,
                contentDescription = callItem.callType.name,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name or Number (Missed calls highlighted in iOS Red)
                Text(
                    text = callItem.displayTitle,
                    color = if (callItem.isMissed) IosRed else primaryTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${callItem.number} ${if (callItem.formattedDuration().isNotEmpty()) "• ${callItem.formattedDuration()}" else ""}".trim(),
                    color = secondaryTextColor,
                    fontSize = 13.sp
                )
            }

            // Timestamp
            Text(
                text = callItem.formattedTime(),
                color = secondaryTextColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            // iOS info circle icon
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Call Info",
                tint = IosBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = separatorColor)
    }
}

private fun getCallTypeIcon(type: CallType): Pair<ImageVector, Color> {
    return when (type) {
        CallType.INCOMING -> Pair(Icons.AutoMirrored.Filled.CallReceived, IosGreen)
        CallType.OUTGOING -> Pair(Icons.AutoMirrored.Filled.CallMade, IosBlue)
        CallType.MISSED, CallType.REJECTED -> Pair(Icons.AutoMirrored.Filled.CallMissed, IosRed)
        CallType.VOICEMAIL -> Pair(Icons.AutoMirrored.Filled.CallReceived, IosBlue)
        else -> Pair(Icons.AutoMirrored.Filled.CallMade, Color.Gray)
    }
}

@Composable
private fun CallLogPermissionDeniedView(
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Call Log Permission Required",
                tint = IosBlue,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Call History Access Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryTextColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "To display your real recent incoming, outgoing, and missed call logs, Android requires the READ_CALL_LOG permission.\n\nIn accordance with Google Play privacy rules, this application operates locally and never manufactures fake call logs or transmits your records off-device.",
                fontSize = 14.sp,
                color = secondaryTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("request_call_log_permission_button")
            ) {
                Text("Allow Call Log Access", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
