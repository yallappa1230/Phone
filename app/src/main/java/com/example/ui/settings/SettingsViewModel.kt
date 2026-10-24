package com.example.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DataStoreManager
import com.example.data.repository.VoicemailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val hasContactsPermission: Boolean = false,
    val hasCallLogPermission: Boolean = false,
    val hasVaultPin: Boolean = false,
    val statusMessage: String? = null
)

class SettingsViewModel(
    private val context: Context,
    val dataStoreManager: DataStoreManager,
    private val voicemailRepository: VoicemailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val themeMode: StateFlow<String> = dataStoreManager.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val hapticEnabled: StateFlow<Boolean> = dataStoreManager.hapticEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoLockMinutes: StateFlow<Int> = dataStoreManager.vaultAutoLockMinutesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    init {
        checkPermissionsAndStatus()
    }

    fun checkPermissionsAndStatus() {
        val contactsGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val callLogGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        viewModelScope.launch {
            val pinHash = dataStoreManager.vaultPinHashFlow.first()
            _uiState.value = _uiState.value.copy(
                hasContactsPermission = contactsGranted,
                hasCallLogPermission = callLogGranted,
                hasVaultPin = !pinHash.isNullOrEmpty()
            )
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStoreManager.setThemeMode(mode)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setHapticEnabled(enabled)
        }
    }

    fun setAutoLockMinutes(minutes: Int) {
        viewModelScope.launch {
            dataStoreManager.setVaultAutoLockMinutes(minutes)
        }
    }

    fun resetVaultPin() {
        viewModelScope.launch {
            dataStoreManager.clearVaultPin()
            voicemailRepository.clearVault()
            _uiState.value = _uiState.value.copy(
                hasVaultPin = false,
                statusMessage = "Vault cleared and PIN reset."
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
