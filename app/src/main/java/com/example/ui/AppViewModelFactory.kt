package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.DataStoreManager
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.VoicemailRepository
import com.example.ui.contacts.ContactsViewModel
import com.example.ui.favorites.FavoritesViewModel
import com.example.ui.keypad.KeypadViewModel
import com.example.ui.recents.RecentsViewModel
import com.example.ui.settings.SettingsViewModel
import com.example.ui.voicemail.VoicemailViewModel

class AppViewModelFactory(
    private val context: Context,
    private val dataStoreManager: DataStoreManager,
    private val contactsRepository: ContactsRepository,
    private val callLogRepository: CallLogRepository,
    private val voicemailRepository: VoicemailRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(KeypadViewModel::class.java) -> {
                KeypadViewModel(context, dataStoreManager) as T
            }
            modelClass.isAssignableFrom(ContactsViewModel::class.java) -> {
                ContactsViewModel(context, contactsRepository) as T
            }
            modelClass.isAssignableFrom(FavoritesViewModel::class.java) -> {
                FavoritesViewModel(context, contactsRepository) as T
            }
            modelClass.isAssignableFrom(RecentsViewModel::class.java) -> {
                RecentsViewModel(context, callLogRepository) as T
            }
            modelClass.isAssignableFrom(VoicemailViewModel::class.java) -> {
                VoicemailViewModel(context, voicemailRepository, dataStoreManager) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(context, dataStoreManager, voicemailRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
