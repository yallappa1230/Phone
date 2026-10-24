package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val senderName: String,
    val senderNumber: String,
    val timestamp: Long,
    val durationSeconds: Int,
    val encryptedFileName: String,
    val ivBase64: String,
    val notes: String = "",
    val isUnread: Boolean = true
)

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY timestamp DESC")
    fun getAllVaultItems(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): VaultItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity)

    @Query("UPDATE vault_items SET isUnread = 0 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Delete
    suspend fun deleteItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM vault_items")
    suspend fun clearVault()
}
