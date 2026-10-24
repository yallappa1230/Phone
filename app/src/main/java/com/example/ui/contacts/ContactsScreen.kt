package com.example.ui.contacts

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.ContactItem
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkBackground
import com.example.ui.theme.IosDarkKeypad
import com.example.ui.theme.IosDarkSeparator
import com.example.ui.theme.IosDarkSurface
import com.example.ui.theme.IosDarkTextPrimary
import com.example.ui.theme.IosDarkTextSecondary
import com.example.ui.theme.IosLightBackground
import com.example.ui.theme.IosLightKeypad
import com.example.ui.theme.IosLightSeparator
import com.example.ui.theme.IosLightSurface
import com.example.ui.theme.IosLightTextPrimary
import com.example.ui.theme.IosLightTextSecondary
import com.example.ui.theme.IosOrange

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val contacts by viewModel.contactsList.collectAsState()

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
    val searchBg = if (isDarkTheme) IosDarkKeypad else IosLightKeypad
    val separatorColor = if (isDarkTheme) IosDarkSeparator else IosLightSeparator

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) IosDarkBackground else IosLightBackground)
    ) {
        // Top App Bar / Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Contacts",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // iOS-styled search box
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = {
                    Text(
                        "Search",
                        color = secondaryTextColor,
                        fontSize = 15.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = searchBg,
                    unfocusedContainerColor = searchBg,
                    disabledContainerColor = searchBg,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("contacts_search_input")
            )
        }

        if (!uiState.hasPermission) {
            // Permission Rationale & Request Flow
            ContactsPermissionDeniedView(
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            )
        } else if (contacts.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "No contacts",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) "No results found for \"${uiState.searchQuery}\"" else "No Contacts Found",
                        color = primaryTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Contacts added on your phone will appear here automatically.",
                        color = secondaryTextColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Contacts List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .testTag("contacts_list")
            ) {
                items(contacts, key = { it.id + it.phoneNumber }) { contact ->
                    ContactRow(
                        contact = contact,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        separatorColor = separatorColor,
                        onClick = { viewModel.selectContact(contact) }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    uiState.selectedContact?.let { contact ->
        ContactDetailDialog(
            contact = contact,
            onDismiss = { viewModel.selectContact(null) },
            onCallClick = { phone ->
                viewModel.selectContact(null)
                viewModel.callContact(phone)
            },
            onMessageClick = { phone ->
                viewModel.selectContact(null)
                viewModel.messageContact(phone)
            },
            onToggleFavorite = { updated ->
                viewModel.toggleFavorite(updated)
            },
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
private fun ContactRow(
    contact: ContactItem,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    separatorColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("contact_item_${contact.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(IosBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.initials,
                    color = IosBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.displayName,
                    color = primaryTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = contact.phoneNumber,
                    color = secondaryTextColor,
                    fontSize = 13.sp
                )
            }

            if (contact.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorited",
                    tint = IosOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = separatorColor)
    }
}

@Composable
private fun ContactsPermissionDeniedView(
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
                contentDescription = "Permission Required",
                tint = IosBlue,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Contacts Access Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryTextColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "To search your phone book, view names, and add favorites, this app requires local read-only access to Android Contacts.\n\nContacts are never uploaded or shared externally.",
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
                modifier = Modifier.testTag("request_contacts_permission_button")
            ) {
                Text("Allow Contacts Access", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
