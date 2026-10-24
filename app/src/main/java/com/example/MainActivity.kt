package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.VoicemailRepository
import com.example.data.security.CryptoManager
import com.example.ui.AppViewModelFactory
import com.example.ui.contacts.ContactsScreen
import com.example.ui.contacts.ContactsViewModel
import com.example.ui.favorites.FavoritesScreen
import com.example.ui.favorites.FavoritesViewModel
import com.example.ui.keypad.KeypadScreen
import com.example.ui.keypad.KeypadViewModel
import com.example.ui.navigation.IosBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.recents.RecentsScreen
import com.example.ui.recents.RecentsViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosDarkBackground
import com.example.ui.theme.IosLightBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.voicemail.VoicemailScreen
import com.example.ui.voicemail.VoicemailViewModel

class MainActivity : ComponentActivity() {

    private lateinit var appDatabase: AppDatabase
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var cryptoManager: CryptoManager
    private lateinit var contactsRepository: ContactsRepository
    private lateinit var callLogRepository: CallLogRepository
    private lateinit var voicemailRepository: VoicemailRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appDatabase = AppDatabase.getInstance(applicationContext)
        dataStoreManager = DataStoreManager(applicationContext)
        cryptoManager = CryptoManager(applicationContext)
        contactsRepository = ContactsRepository(applicationContext, appDatabase.favoriteDao())
        callLogRepository = CallLogRepository(applicationContext)
        voicemailRepository = VoicemailRepository(applicationContext, appDatabase.vaultDao(), cryptoManager)

        setContent {
            val themeMode by dataStoreManager.themeModeFlow.collectAsState(initial = "SYSTEM")
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppView(
                    factory = AppViewModelFactory(
                        context = applicationContext,
                        dataStoreManager = dataStoreManager,
                        contactsRepository = contactsRepository,
                        callLogRepository = callLogRepository,
                        voicemailRepository = voicemailRepository
                    ),
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun MainAppView(
    factory: AppViewModelFactory,
    isDarkTheme: Boolean
) {
    var currentScreen by remember { mutableStateOf(Screen.KEYPAD) }
    var showSettings by remember { mutableStateOf(false) }

    val keypadViewModel: KeypadViewModel = viewModel(factory = factory)
    val contactsViewModel: ContactsViewModel = viewModel(factory = factory)
    val favoritesViewModel: FavoritesViewModel = viewModel(factory = factory)
    val recentsViewModel: RecentsViewModel = viewModel(factory = factory)
    val voicemailViewModel: VoicemailViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    // Back button handling
    BackHandler(enabled = showSettings || currentScreen != Screen.KEYPAD) {
        if (showSettings) {
            showSettings = false
        } else {
            currentScreen = Screen.KEYPAD
        }
    }

    val backgroundColor = if (isDarkTheme) IosDarkBackground else IosLightBackground

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        bottomBar = {
            if (!showSettings) {
                IosBottomBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        currentScreen = screen
                    },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            if (showSettings) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { showSettings = false },
                    isDarkTheme = isDarkTheme
                )
            } else {
                when (currentScreen) {
                    Screen.FAVORITES -> {
                        FavoritesScreen(
                            viewModel = favoritesViewModel,
                            onNavigateToContacts = { currentScreen = Screen.CONTACTS },
                            isDarkTheme = isDarkTheme
                        )
                    }
                    Screen.RECENTS -> {
                        RecentsScreen(
                            viewModel = recentsViewModel,
                            isDarkTheme = isDarkTheme
                        )
                    }
                    Screen.CONTACTS -> {
                        ContactsScreen(
                            viewModel = contactsViewModel,
                            isDarkTheme = isDarkTheme
                        )
                    }
                    Screen.KEYPAD -> {
                        KeypadScreen(
                            viewModel = keypadViewModel,
                            isDarkTheme = isDarkTheme
                        )
                    }
                    Screen.VOICEMAIL -> {
                        VoicemailScreen(
                            viewModel = voicemailViewModel,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                // Top right Settings gear icon overlay on screens where appropriate (e.g. Keypad)
                if (currentScreen == Screen.KEYPAD) {
                    IconButton(
                        onClick = {
                            settingsViewModel.checkPermissionsAndStatus()
                            showSettings = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, top = 4.dp)
                            .testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = IosBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
