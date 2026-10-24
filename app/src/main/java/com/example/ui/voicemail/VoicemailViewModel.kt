package com.example.ui.voicemail

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DataStoreManager
import com.example.data.local.VaultItemEntity
import com.example.data.model.CarrierVoicemailConfig
import com.example.data.repository.SimCarrierInfo
import com.example.data.repository.VisualVoicemailDiagnostic
import com.example.data.repository.VoicemailRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class VoicemailSection {
    CARRIER_VOICEMAIL,
    VISUAL_VOICEMAIL,
    PRIVATE_VAULT
}

enum class VaultLockState {
    NO_PIN_CONFIGURED,
    LOCKED,
    UNLOCKED,
    LOCKED_OUT
}

data class VoicemailUiState(
    val currentSection: VoicemailSection = VoicemailSection.CARRIER_VOICEMAIL,
    val selectedSimSlot: Int = 0,
    val detectedSims: List<SimCarrierInfo> = emptyList(),
    val sim1Number: String = "*86",
    val sim2Number: String = "*86",
    val sim1CarrierName: String = "SIM 1",
    val sim2CarrierName: String = "SIM 2",
    val visualVoicemailDiagnostic: VisualVoicemailDiagnostic? = null,
    val vaultLockState: VaultLockState = VaultLockState.LOCKED,
    val failedAttempts: Int = 0,
    val lockoutSecondsRemaining: Int = 0,
    val currentlyPlayingItemId: String? = null,
    val isPlaying: Boolean = false,
    val playbackProgressMs: Int = 0,
    val playbackDurationMs: Int = 0,
    val statusMessage: String? = null
)

class VoicemailViewModel(
    private val context: Context,
    private val voicemailRepository: VoicemailRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoicemailUiState())
    val uiState: StateFlow<VoicemailUiState> = _uiState.asStateFlow()

    val vaultItems: StateFlow<List<VaultItemEntity>> = voicemailRepository.getVaultItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var mediaPlayer: MediaPlayer? = null
    private var playbackProgressJob: Job? = null
    private var autoLockJob: Job? = null
    private var currentTempAudioFile: File? = null

    init {
        loadSettingsAndDetectSims()
        checkVaultPinStatus()
    }

    fun setSection(section: VoicemailSection) {
        _uiState.value = _uiState.value.copy(currentSection = section)
        if (section == VoicemailSection.VISUAL_VOICEMAIL) {
            refreshVisualVoicemailDiagnostic()
        }
    }

    fun setSelectedSimSlot(slot: Int) {
        _uiState.value = _uiState.value.copy(selectedSimSlot = slot)
    }

    fun loadSettingsAndDetectSims() {
        viewModelScope.launch {
            val sims = voicemailRepository.detectSimCarriers()
            val sim1Num = dataStoreManager.voicemailSim1Flow.first()
            val sim2Num = dataStoreManager.voicemailSim2Flow.first()
            val sim1Carrier = dataStoreManager.carrierNameSim1Flow.first()
            val sim2Carrier = dataStoreManager.carrierNameSim2Flow.first()

            val detectedSim1Number = sims.getOrNull(0)?.carrierVoicemailNumber ?: sim1Num
            val detectedSim2Number = sims.getOrNull(1)?.carrierVoicemailNumber ?: sim2Num

            _uiState.value = _uiState.value.copy(
                detectedSims = sims,
                sim1Number = detectedSim1Number,
                sim2Number = detectedSim2Number,
                sim1CarrierName = sims.getOrNull(0)?.carrierName ?: sim1Carrier,
                sim2CarrierName = sims.getOrNull(1)?.carrierName ?: sim2Carrier
            )
            refreshVisualVoicemailDiagnostic()
        }
    }

    fun refreshVisualVoicemailDiagnostic() {
        val diagnostic = voicemailRepository.inspectVisualVoicemail()
        _uiState.value = _uiState.value.copy(visualVoicemailDiagnostic = diagnostic)
    }

    fun updateCarrierNumber(slot: Int, number: String, carrierName: String? = null) {
        viewModelScope.launch {
            if (slot == 0) {
                dataStoreManager.setVoicemailSim1(number)
                carrierName?.let { dataStoreManager.setCarrierNameSim1(it) }
                _uiState.value = _uiState.value.copy(
                    sim1Number = number,
                    sim1CarrierName = carrierName ?: _uiState.value.sim1CarrierName
                )
            } else {
                dataStoreManager.setVoicemailSim2(number)
                carrierName?.let { dataStoreManager.setCarrierNameSim2(it) }
                _uiState.value = _uiState.value.copy(
                    sim2Number = number,
                    sim2CarrierName = carrierName ?: _uiState.value.sim2CarrierName
                )
            }
        }
    }

    fun applyPreset(preset: CarrierVoicemailConfig, slot: Int) {
        updateCarrierNumber(slot, preset.defaultAccessNumber, preset.carrierName)
        _uiState.value = _uiState.value.copy(
            statusMessage = "Applied ${preset.carrierName} access number (${preset.defaultAccessNumber})"
        )
    }

    fun callVoicemail() {
        val number = if (_uiState.value.selectedSimSlot == 0) {
            _uiState.value.sim1Number
        } else {
            _uiState.value.sim2Number
        }

        if (number.isBlank()) {
            _uiState.value = _uiState.value.copy(statusMessage = "No voicemail number configured for this SIM.")
            return
        }

        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(number.trim())}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(statusMessage = "Unable to open system dialer.")
        }
    }

    // --- Private Vault PIN and Locking ---

    private fun checkVaultPinStatus() {
        viewModelScope.launch {
            val hash = dataStoreManager.vaultPinHashFlow.first()
            val lockoutUntil = dataStoreManager.vaultLockoutUntilFlow.first()
            val now = System.currentTimeMillis()

            if (hash.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(vaultLockState = VaultLockState.NO_PIN_CONFIGURED)
            } else if (lockoutUntil > now) {
                val remSeconds = ((lockoutUntil - now) / 1000).toInt()
                _uiState.value = _uiState.value.copy(
                    vaultLockState = VaultLockState.LOCKED_OUT,
                    lockoutSecondsRemaining = remSeconds
                )
                startLockoutCountdown(remSeconds)
            } else {
                _uiState.value = _uiState.value.copy(vaultLockState = VaultLockState.LOCKED)
            }
        }
    }

    fun setupPin(newPin: String) {
        viewModelScope.launch {
            val salt = voicemailRepository.cryptoManager.generateSalt()
            val hash = voicemailRepository.cryptoManager.hashPin(newPin, salt)
            dataStoreManager.setVaultPin(hash, salt)
            _uiState.value = _uiState.value.copy(
                vaultLockState = VaultLockState.UNLOCKED,
                statusMessage = "PIN configured successfully. Vault unlocked."
            )
            scheduleAutoLock()
        }
    }

    fun unlockVault(enteredPin: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val hash = dataStoreManager.vaultPinHashFlow.first()
            val salt = dataStoreManager.vaultPinSaltFlow.first()

            if (hash.isNullOrEmpty() || salt.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(vaultLockState = VaultLockState.NO_PIN_CONFIGURED)
                onResult(false, "No PIN setup yet.")
                return@launch
            }

            val isValid = voicemailRepository.cryptoManager.verifyPin(enteredPin, hash, salt)
            if (isValid) {
                dataStoreManager.resetFailedAttempts()
                _uiState.value = _uiState.value.copy(
                    vaultLockState = VaultLockState.UNLOCKED,
                    failedAttempts = 0
                )
                scheduleAutoLock()
                onResult(true, null)
            } else {
                val failed = (dataStoreManager.vaultFailedAttemptsFlow.first()) + 1
                if (failed >= 5) {
                    val lockoutDuration = if (failed >= 10) 300_000L else 30_000L
                    val lockoutUntil = System.currentTimeMillis() + lockoutDuration
                    dataStoreManager.recordFailedAttempt(lockoutUntil)
                    val remSeconds = (lockoutDuration / 1000).toInt()
                    _uiState.value = _uiState.value.copy(
                        vaultLockState = VaultLockState.LOCKED_OUT,
                        lockoutSecondsRemaining = remSeconds,
                        failedAttempts = failed
                    )
                    startLockoutCountdown(remSeconds)
                    onResult(false, "Too many failed attempts. Locked for $remSeconds seconds.")
                } else {
                    dataStoreManager.recordFailedAttempt()
                    _uiState.value = _uiState.value.copy(failedAttempts = failed)
                    val remaining = 5 - failed
                    onResult(false, "Incorrect PIN. $remaining attempts remaining before lockout.")
                }
            }
        }
    }

    private fun startLockoutCountdown(seconds: Int) {
        viewModelScope.launch {
            var rem = seconds
            while (rem > 0) {
                delay(1000)
                rem -= 1
                _uiState.value = _uiState.value.copy(lockoutSecondsRemaining = rem)
            }
            _uiState.value = _uiState.value.copy(vaultLockState = VaultLockState.LOCKED)
        }
    }

    fun lockVault() {
        stopAudioPlayback()
        _uiState.value = _uiState.value.copy(vaultLockState = VaultLockState.LOCKED)
    }

    private fun scheduleAutoLock() {
        autoLockJob?.cancel()
        autoLockJob = viewModelScope.launch {
            val minutes = dataStoreManager.vaultAutoLockMinutesFlow.first()
            delay(minutes * 60 * 1000L)
            lockVault()
        }
    }

    // --- Audio Playback and Vault Management ---

    fun playVaultAudio(item: VaultItemEntity) {
        if (_uiState.value.currentlyPlayingItemId == item.id && _uiState.value.isPlaying) {
            mediaPlayer?.pause()
            _uiState.value = _uiState.value.copy(isPlaying = false)
            return
        }

        if (_uiState.value.currentlyPlayingItemId == item.id && !_uiState.value.isPlaying && mediaPlayer != null) {
            mediaPlayer?.start()
            _uiState.value = _uiState.value.copy(isPlaying = true)
            return
        }

        // New item playback: Decrypt to temp file
        stopAudioPlayback()
        viewModelScope.launch {
            try {
                val tempFile = voicemailRepository.cryptoManager.decryptFileToTemp(
                    item.encryptedFileName,
                    item.ivBase64
                )
                currentTempAudioFile = tempFile

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        stopAudioPlayback()
                    }
                }

                voicemailRepository.markAsRead(item.id)

                val duration = mediaPlayer?.duration ?: (item.durationSeconds * 1000)
                _uiState.value = _uiState.value.copy(
                    currentlyPlayingItemId = item.id,
                    isPlaying = true,
                    playbackDurationMs = duration,
                    playbackProgressMs = 0
                )

                startProgressTracker()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Unable to play audio: Corrupted file or key mismatch."
                )
            }
        }
    }

    private fun startProgressTracker() {
        playbackProgressJob?.cancel()
        playbackProgressJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                val current = mediaPlayer?.currentPosition ?: 0
                _uiState.value = _uiState.value.copy(playbackProgressMs = current)
                delay(200)
            }
        }
    }

    fun stopAudioPlayback() {
        playbackProgressJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentTempAudioFile?.delete()
        currentTempAudioFile = null
        _uiState.value = _uiState.value.copy(
            currentlyPlayingItemId = null,
            isPlaying = false,
            playbackProgressMs = 0,
            playbackDurationMs = 0
        )
    }

    fun addDemoSecureVoicemail(title: String, senderName: String, senderNumber: String, notes: String) {
        viewModelScope.launch {
            // Generate a synthetic voice note audio chunk for testing private vault
            val dummyAudio = ByteArray(4096) { (it % 128).toByte() }
            voicemailRepository.createVaultVoiceMemo(
                title = title,
                senderName = senderName,
                senderNumber = senderNumber,
                audioBytes = dummyAudio,
                durationSeconds = 18,
                notes = notes
            )
            _uiState.value = _uiState.value.copy(statusMessage = "Encrypted message saved to vault.")
        }
    }

    fun deleteVaultItem(item: VaultItemEntity) {
        viewModelScope.launch {
            if (_uiState.value.currentlyPlayingItemId == item.id) {
                stopAudioPlayback()
            }
            voicemailRepository.deleteVaultItem(item)
            _uiState.value = _uiState.value.copy(statusMessage = "Message securely wiped from device.")
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopAudioPlayback()
        voicemailRepository.cryptoManager.clearTempDecryptedFiles()
    }
}
