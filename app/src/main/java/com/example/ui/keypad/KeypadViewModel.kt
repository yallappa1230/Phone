package com.example.ui.keypad

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class KeypadUiState(
    val enteredNumber: String = "",
    val errorMessage: String? = null,
    val showAddContactPrompt: Boolean = false
)

data class KeypadKey(
    val digit: String,
    val letters: String = "",
    val isAction: Boolean = false
)

class KeypadViewModel(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(KeypadUiState())
    val uiState: StateFlow<KeypadUiState> = _uiState.asStateFlow()

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun onDigitClick(digit: String) {
        triggerHaptic()
        _uiState.value = _uiState.value.copy(
            enteredNumber = _uiState.value.enteredNumber + digit,
            errorMessage = null
        )
    }

    fun onZeroLongClick() {
        triggerHaptic(strong = true)
        _uiState.value = _uiState.value.copy(
            enteredNumber = _uiState.value.enteredNumber + "+",
            errorMessage = null
        )
    }

    fun onBackspaceClick() {
        triggerHaptic()
        val current = _uiState.value.enteredNumber
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                enteredNumber = current.dropLast(1),
                errorMessage = null
            )
        }
    }

    fun onBackspaceLongClick() {
        triggerHaptic(strong = true)
        _uiState.value = _uiState.value.copy(
            enteredNumber = "",
            errorMessage = null
        )
    }

    fun setNumber(number: String) {
        val sanitized = number.filter { it.isDigit() || it == '*' || it == '#' || it == '+' }
        _uiState.value = _uiState.value.copy(
            enteredNumber = sanitized,
            errorMessage = null
        )
    }

    fun clearNumber() {
        _uiState.value = _uiState.value.copy(enteredNumber = "")
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun initiateCall(numberToCall: String? = null) {
        val targetNumber = numberToCall ?: _uiState.value.enteredNumber
        if (targetNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a phone number first.")
            return
        }

        triggerHaptic(strong = true)

        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(targetNumber.trim())}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No compatible system dialer found to place this call."
            )
        }
    }

    private fun triggerHaptic(strong: Boolean = false) {
        viewModelScope.launch {
            val enabled = dataStoreManager.hapticEnabledFlow.first()
            if (!enabled) return@launch

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val duration = if (strong) 35L else 18L
                    val amplitude = if (strong) VibrationEffect.DEFAULT_AMPLITUDE else 80
                    vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(if (strong) 35L else 18L)
                }
            } catch (_: Exception) {}
        }
    }
}
