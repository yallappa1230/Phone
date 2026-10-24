package com.example.ui.recents

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CallLogItem
import com.example.data.repository.CallLogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class RecentsFilter {
    ALL,
    MISSED
}

data class RecentsUiState(
    val filter: RecentsFilter = RecentsFilter.ALL,
    val hasPermission: Boolean = false,
    val missedCount: Int = 0
)

class RecentsViewModel(
    private val context: Context,
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RecentsUiState(hasPermission = callLogRepository.hasCallLogPermission())
    )
    val uiState: StateFlow<RecentsUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow(RecentsFilter.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val callLogs: StateFlow<List<CallLogItem>> = _filter
        .flatMapLatest { filter ->
            callLogRepository.getCallLogs(filterMissedOnly = filter == RecentsFilter.MISSED)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setFilter(filter: RecentsFilter) {
        _filter.value = filter
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.value = _uiState.value.copy(hasPermission = isGranted)
        if (isGranted) {
            refresh()
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(
            hasPermission = callLogRepository.hasCallLogPermission()
        )
        // re-trigger
        _filter.value = _uiState.value.filter
    }

    fun callBack(phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(phoneNumber.trim())}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
