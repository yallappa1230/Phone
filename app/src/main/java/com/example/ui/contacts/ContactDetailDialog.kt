package com.example.ui.contacts

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailDialog(
    contact: ContactItem,
    onDismiss: () -> Unit,
    onCallClick: (String) -> Unit,
    onMessageClick: (String) -> Unit,
    onToggleFavorite: (ContactItem) -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val surfaceColor = if (isDarkTheme) IosDarkSurface else IosLightSurface
    val primaryText = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryText = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val separatorColor = if (isDarkTheme) IosDarkSeparator else IosLightSeparator

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = secondaryText
                    )
                }
            }

            // Initials Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(IosBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.initials,
                    color = IosBlue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Name
            Text(
                text = contact.displayName,
                color = primaryText,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Phone Number
            Text(
                text = contact.phoneNumber,
                color = secondaryText,
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Pills Row: Call | Message | Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Call Pill
                IosContactActionButton(
                    icon = Icons.Default.Call,
                    label = "Call",
                    iconColor = IosGreen,
                    backgroundColor = if (isDarkTheme) IosDarkBackground else IosLightBackground,
                    textColor = primaryText,
                    onClick = { onCallClick(contact.phoneNumber) },
                    modifier = Modifier.testTag("contact_detail_call")
                )

                // Message Pill
                IosContactActionButton(
                    icon = Icons.AutoMirrored.Filled.Message,
                    label = "Message",
                    iconColor = IosBlue,
                    backgroundColor = if (isDarkTheme) IosDarkBackground else IosLightBackground,
                    textColor = primaryText,
                    onClick = { onMessageClick(contact.phoneNumber) },
                    modifier = Modifier.testTag("contact_detail_message")
                )

                // Favorite Pill
                IosContactActionButton(
                    icon = if (contact.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    label = if (contact.isFavorite) "Favorited" else "Favorite",
                    iconColor = IosOrange,
                    backgroundColor = if (isDarkTheme) IosDarkBackground else IosLightBackground,
                    textColor = primaryText,
                    onClick = { onToggleFavorite(contact) },
                    modifier = Modifier.testTag("contact_detail_fav")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = separatorColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Informational privacy badge
            Text(
                text = "Contact data is stored on-device and never uploaded.",
                color = secondaryText,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IosContactActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconColor: Color,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
