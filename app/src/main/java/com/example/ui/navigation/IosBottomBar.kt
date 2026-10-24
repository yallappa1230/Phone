package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkBottomBar
import com.example.ui.theme.IosDarkSeparator
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosLightBottomBar
import com.example.ui.theme.IosLightSeparator
import com.example.ui.theme.IosLightTextSecondary
import com.example.ui.theme.IosRed

@Composable
fun IosBottomBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    missedCallsCount: Int = 0,
    unreadVoicemailCount: Int = 0,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val barColor = if (isDarkTheme) IosDarkBottomBar else IosLightBottomBar
    val separatorColor = if (isDarkTheme) IosDarkSeparator else IosLightSeparator
    val inactiveColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(barColor)
    ) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = separatorColor
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Screen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                val (selectedIcon, unselectedIcon) = getScreenIcons(screen)
                val badgeCount = when (screen) {
                    Screen.RECENTS -> missedCallsCount
                    Screen.VOICEMAIL -> unreadVoicemailCount
                    else -> 0
                }

                IosBottomTabItem(
                    screen = screen,
                    isSelected = isSelected,
                    selectedIcon = selectedIcon,
                    unselectedIcon = unselectedIcon,
                    badgeCount = badgeCount,
                    activeColor = IosBlue,
                    inactiveColor = inactiveColor,
                    onClick = { onScreenSelected(screen) }
                )
            }
        }
    }
}

@Composable
private fun IosBottomTabItem(
    screen: Screen,
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    badgeCount: Int,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tint = if (isSelected) activeColor else inactiveColor

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("tab_${screen.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = screen.title,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-6).dp)
                        .size(16.dp)
                        .background(IosRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = screen.title,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun getScreenIcons(screen: Screen): Pair<ImageVector, ImageVector> {
    return when (screen) {
        Screen.FAVORITES -> Pair(Icons.Filled.Star, Icons.Outlined.StarOutline)
        Screen.RECENTS -> Pair(Icons.Filled.Schedule, Icons.Outlined.Schedule)
        Screen.CONTACTS -> Pair(Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
        Screen.KEYPAD -> Pair(Icons.Filled.Dialpad, Icons.Outlined.Dialpad)
        Screen.VOICEMAIL -> Pair(Icons.Filled.Voicemail, Icons.Outlined.Voicemail)
    }
}
