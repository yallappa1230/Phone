package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "phone_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val VOICEMAIL_SIM1 = stringPreferencesKey("voicemail_sim1")
        val VOICEMAIL_SIM2 = stringPreferencesKey("voicemail_sim2")
        val CARRIER_NAME_SIM1 = stringPreferencesKey("carrier_name_sim1")
        val CARRIER_NAME_SIM2 = stringPreferencesKey("carrier_name_sim2")
        
        // Vault Settings
        val VAULT_PIN_HASH = stringPreferencesKey("vault_pin_hash")
        val VAULT_PIN_SALT = stringPreferencesKey("vault_pin_salt")
        val VAULT_FAILED_ATTEMPTS = intPreferencesKey("vault_failed_attempts")
        val VAULT_LOCKOUT_UNTIL = longPreferencesKey("vault_lockout_until")
        val VAULT_AUTO_LOCK_MINUTES = intPreferencesKey("vault_auto_lock_minutes")
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "SYSTEM"
    }

    val hapticEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTIC_ENABLED] ?: true
    }

    val voicemailSim1Flow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[VOICEMAIL_SIM1] ?: "*86"
    }

    val voicemailSim2Flow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[VOICEMAIL_SIM2] ?: "*86"
    }

    val carrierNameSim1Flow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CARRIER_NAME_SIM1] ?: "SIM 1"
    }

    val carrierNameSim2Flow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CARRIER_NAME_SIM2] ?: "SIM 2"
    }

    val vaultPinHashFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[VAULT_PIN_HASH]
    }

    val vaultPinSaltFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[VAULT_PIN_SALT]
    }

    val vaultFailedAttemptsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[VAULT_FAILED_ATTEMPTS] ?: 0
    }

    val vaultLockoutUntilFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[VAULT_LOCKOUT_UNTIL] ?: 0L
    }

    val vaultAutoLockMinutesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[VAULT_AUTO_LOCK_MINUTES] ?: 2
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setVoicemailSim1(number: String) {
        context.dataStore.edit { preferences ->
            preferences[VOICEMAIL_SIM1] = number
        }
    }

    suspend fun setVoicemailSim2(number: String) {
        context.dataStore.edit { preferences ->
            preferences[VOICEMAIL_SIM2] = number
        }
    }

    suspend fun setCarrierNameSim1(name: String) {
        context.dataStore.edit { preferences ->
            preferences[CARRIER_NAME_SIM1] = name
        }
    }

    suspend fun setCarrierNameSim2(name: String) {
        context.dataStore.edit { preferences ->
            preferences[CARRIER_NAME_SIM2] = name
        }
    }

    suspend fun setVaultPin(hash: String, salt: String) {
        context.dataStore.edit { preferences ->
            preferences[VAULT_PIN_HASH] = hash
            preferences[VAULT_PIN_SALT] = salt
            preferences[VAULT_FAILED_ATTEMPTS] = 0
            preferences[VAULT_LOCKOUT_UNTIL] = 0L
        }
    }

    suspend fun recordFailedAttempt(lockoutUntil: Long = 0L) {
        context.dataStore.edit { preferences ->
            val current = preferences[VAULT_FAILED_ATTEMPTS] ?: 0
            preferences[VAULT_FAILED_ATTEMPTS] = current + 1
            if (lockoutUntil > 0L) {
                preferences[VAULT_LOCKOUT_UNTIL] = lockoutUntil
            }
        }
    }

    suspend fun resetFailedAttempts() {
        context.dataStore.edit { preferences ->
            preferences[VAULT_FAILED_ATTEMPTS] = 0
            preferences[VAULT_LOCKOUT_UNTIL] = 0L
        }
    }

    suspend fun setVaultAutoLockMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[VAULT_AUTO_LOCK_MINUTES] = minutes
        }
    }

    suspend fun clearVaultPin() {
        context.dataStore.edit { preferences ->
            preferences.remove(VAULT_PIN_HASH)
            preferences.remove(VAULT_PIN_SALT)
            preferences[VAULT_FAILED_ATTEMPTS] = 0
            preferences[VAULT_LOCKOUT_UNTIL] = 0L
        }
    }
}
