package com.example.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteEntity
import com.example.data.model.ContactItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ContactsRepository(
    private val context: Context,
    private val favoriteDao: FavoriteDao
) {
    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    suspend fun addFavorite(contact: ContactItem) {
        favoriteDao.addFavorite(
            FavoriteEntity(
                contactId = contact.id,
                displayName = contact.displayName,
                phoneNumber = contact.phoneNumber,
                photoUri = contact.photoUri
            )
        )
    }

    suspend fun removeFavorite(contactId: String) {
        favoriteDao.removeFavoriteById(contactId)
    }

    fun getContacts(searchQuery: String = ""): Flow<List<ContactItem>> = flow {
        if (!hasContactsPermission()) {
            emit(emptyList())
            return@flow
        }

        val contactsList = mutableListOf<ContactItem>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        val selection = if (searchQuery.isNotBlank()) {
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        } else {
            null
        }

        val selectionArgs = if (searchQuery.isNotBlank()) {
            arrayOf("%$searchQuery%", "%$searchQuery%")
        } else {
            null
        }

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val keyIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                val seenIds = mutableSetOf<String>()
                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getString(idIdx) ?: "" else ""
                    val lookupKey = if (keyIdx >= 0) it.getString(keyIdx) ?: "" else ""
                    val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "Unknown" else "Unknown"
                    val number = if (numberIdx >= 0) it.getString(numberIdx) ?: "" else ""
                    val photoUri = if (photoIdx >= 0) it.getString(photoIdx) else null

                    // Avoid duplicate phone entries for same contact display in list
                    val uniqueKey = "$id-$number"
                    if (!seenIds.contains(uniqueKey) && number.isNotBlank()) {
                        seenIds.add(uniqueKey)
                        contactsList.add(
                            ContactItem(
                                id = id,
                                lookupKey = lookupKey,
                                displayName = name,
                                phoneNumber = number,
                                photoUri = photoUri
                            )
                        )
                    }
                }
            }
        } catch (_: SecurityException) {
            emit(emptyList())
            return@flow
        } catch (_: Exception) {
            // Log/graceful recovery
        }

        emit(contactsList)
    }.combine(favoriteDao.getAllFavorites()) { contacts, favorites ->
        val favoriteIds = favorites.map { it.contactId }.toSet()
        contacts.map { contact ->
            contact.copy(isFavorite = favoriteIds.contains(contact.id))
        }
    }.flowOn(Dispatchers.IO)
}
