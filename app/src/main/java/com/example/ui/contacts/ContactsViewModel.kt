package com.example.ui.contacts

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactItem
import com.example.data.repository.ContactsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ContactsUiState(
    val searchQuery: String = "",
    val selectedContact: ContactItem? = null,
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ContactsViewModel(
    private val context: Context,
    private val contactsRepository: ContactsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ContactsUiState(hasPermission = contactsRepository.hasContactsPermission())
    )
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val contactsList: StateFlow<List<ContactItem>> = _searchQuery
        .flatMapLatest { query ->
            contactsRepository.getContacts(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.value = _uiState.value.copy(hasPermission = isGranted)
        if (isGranted) {
            refresh()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun refresh() {
        val hasPerm = contactsRepository.hasContactsPermission()
        _uiState.value = _uiState.value.copy(hasPermission = hasPerm)
        // Trigger flow update
        _searchQuery.value = _uiState.value.searchQuery
    }

    fun selectContact(contact: ContactItem?) {
        _uiState.value = _uiState.value.copy(selectedContact = contact)
    }

    fun toggleFavorite(contact: ContactItem) {
        viewModelScope.launch {
            if (contact.isFavorite) {
                contactsRepository.removeFavorite(contact.id)
            } else {
                contactsRepository.addFavorite(contact)
            }
            // Update selected contact if open
            if (_uiState.value.selectedContact?.id == contact.id) {
                _uiState.value = _uiState.value.copy(
                    selectedContact = contact.copy(isFavorite = !contact.isFavorite)
                )
            }
        }
    }

    fun callContact(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(phoneNumber.trim())}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun messageContact(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(phoneNumber.trim())}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
