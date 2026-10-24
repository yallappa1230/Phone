package com.example.ui.favorites

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FavoriteEntity
import com.example.data.model.ContactItem
import com.example.ui.navigation.Screen
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkBackground
import com.example.ui.theme.IosDarkSeparator
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosLightBackground
import com.example.ui.theme.IosLightSeparator
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosRed

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateToContacts: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()

    val primaryTextColor = if (isDarkTheme) IosDarkTextPrimary else IosLightTextPrimary
    val secondaryTextColor = if (isDarkTheme) IosDarkTextSecondary else IosLightTextSecondary
    val separatorColor = if (isDarkTheme) IosDarkSeparator else IosLightSeparator

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) IosDarkBackground else IosLightBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favorites",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
            IconButton(
                onClick = onNavigateToContacts,
                modifier = Modifier.testTag("add_favorite_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Favorite",
                    tint = IosBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        if (favorites.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "No Favorites",
                        tint = IosOrange.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Favorites Yet",
                        color = primaryTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button or open any contact in the Contacts tab to add them to your one-tap favorites.",
                        color = secondaryTextColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .testTag("favorites_list")
            ) {
                items(favorites, key = { it.contactId }) { favorite ->
                    FavoriteRow(
                        favorite = favorite,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        separatorColor = separatorColor,
                        onCallClick = { viewModel.callFavorite(favorite.phoneNumber) },
                        onRemoveClick = { viewModel.removeFavorite(favorite.contactId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    favorite: FavoriteEntity,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    separatorColor: Color,
    onCallClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCallClick)
            .testTag("favorite_item_${favorite.contactId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(IosOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ContactItem.computeInitials(favorite.displayName),
                    color = IosOrange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.displayName,
                    color = primaryTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = favorite.phoneNumber,
                    color = secondaryTextColor,
                    fontSize = 13.sp
                )
            }

            // Quick Call Button
            IconButton(
                onClick = onCallClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call ${favorite.displayName}",
                    tint = IosGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Remove Button
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove Favorite",
                    tint = secondaryTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = separatorColor)
    }
}
