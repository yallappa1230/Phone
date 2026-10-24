package com.example.data.repository

import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.example.data.local.VaultDao
import com.example.data.local.VaultItemEntity
import com.example.data.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class SimCarrierInfo(
    val simSlotIndex: Int,
    val carrierName: String,
    val carrierVoicemailNumber: String?,
    val isVisualVoicemailSupported: Boolean
)

data class VisualVoicemailDiagnostic(
    val isDefaultDialer: Boolean,
    val hasTelephonyVoicemailNumber: Boolean,
    val detectedVoicemailNumber: String?,
    val carrierName: String,
    val statusSummary: String,
    val technicalExplanation: String
)

class VoicemailRepository(
    private val context: Context,
    private val vaultDao: VaultDao,
    val cryptoManager: CryptoManager
) {
    private val telephonyManager: TelephonyManager? =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val telecomManager: TelecomManager? =
        context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager

    fun isDefaultDialer(): Boolean {
        return try {
            telecomManager?.defaultDialerPackage == context.packageName
        } catch (_: Exception) {
            false
        }
    }

    fun detectSimCarriers(): List<SimCarrierInfo> {
        val result = mutableListOf<SimCarrierInfo>()
        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            val activeList: List<SubscriptionInfo>? = subscriptionManager?.activeSubscriptionInfoList

            if (!activeList.isNullOrEmpty()) {
                for (info in activeList) {
                    val subTelephony = telephonyManager?.createForSubscriptionId(info.subscriptionId)
                    val vmNumber = try {
                        subTelephony?.voiceMailNumber?.takeIf { it.isNotBlank() }
                    } catch (_: SecurityException) {
                        null
                    }

                    result.add(
                        SimCarrierInfo(
                            simSlotIndex = info.simSlotIndex,
                            carrierName = info.carrierName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                            carrierVoicemailNumber = vmNumber,
                            isVisualVoicemailSupported = false
                        )
                    )
                }
            } else {
                // Fallback to primary telephony manager
                val carrier = telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() } ?: "Cellular Carrier"
                val vm = try {
                    telephonyManager?.voiceMailNumber?.takeIf { it.isNotBlank() }
                } catch (_: SecurityException) {
                    null
                }
                result.add(
                    SimCarrierInfo(
                        simSlotIndex = 0,
                        carrierName = carrier,
                        carrierVoicemailNumber = vm,
                        isVisualVoicemailSupported = false
                    )
                )
            }
        } catch (_: Exception) {
            result.add(
                SimCarrierInfo(
                    simSlotIndex = 0,
                    carrierName = "Carrier (SIM 1)",
                    carrierVoicemailNumber = null,
                    isVisualVoicemailSupported = false
                )
            )
        }
        return result
    }

    fun inspectVisualVoicemail(): VisualVoicemailDiagnostic {
        val isDefault = isDefaultDialer()
        val detectedNumber = try {
            telephonyManager?.voiceMailNumber
        } catch (_: SecurityException) {
            null
        }
        val carrier = telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() } ?: "Cellular Carrier"

        val summary: String
        val explanation: String

        if (!isDefault) {
            summary = "Visual Voicemail is restricted to the system default dialer."
            explanation = "Under Android's telecom security architecture (android.provider.VoicemailContract), visual voicemail content providers and background SMS synch protocols are strictly gated behind the Default Dialer role or carrier-signed packages. Because this application operates safely as a companion dialer using ACTION_DIAL handoff without taking over your system telephony role, messages remain secure on your carrier's network."
        } else {
            summary = "App is running in dialer mode. Carrier Visual Voicemail sync protocol is inactive."
            explanation = "While running with default dialer privileges, carriers like Jio and Airtel in India use proprietary IMS/SMS push endpoints (OMTP/3GPP standards) that require carrier provisioning."
        }

        return VisualVoicemailDiagnostic(
            isDefaultDialer = isDefault,
            hasTelephonyVoicemailNumber = !detectedNumber.isNullOrBlank(),
            detectedVoicemailNumber = detectedNumber,
            carrierName = carrier,
            statusSummary = summary,
            technicalExplanation = explanation
        )
    }

    // --- Private Vault Operations ---

    fun getVaultItems(): Flow<List<VaultItemEntity>> = vaultDao.getAllVaultItems()

    suspend fun saveToVault(
        title: String,
        senderName: String,
        senderNumber: String,
        durationSeconds: Int,
        audioSourceFile: File,
        notes: String = ""
    ): VaultItemEntity = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val destFileName = "vault_enc_$id.bin"
        val (encFileName, ivBase64) = cryptoManager.encryptFile(audioSourceFile, destFileName)

        val entity = VaultItemEntity(
            id = id,
            title = title,
            senderName = senderName,
            senderNumber = senderNumber,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            encryptedFileName = encFileName,
            ivBase64 = ivBase64,
            notes = notes,
            isUnread = true
        )
        vaultDao.insertItem(entity)
        entity
    }

    suspend fun createVaultVoiceMemo(
        title: String,
        senderName: String,
        senderNumber: String,
        audioBytes: ByteArray,
        durationSeconds: Int,
        notes: String = ""
    ): VaultItemEntity = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("memo_in_", ".m4a", context.cacheDir)
        tempFile.writeBytes(audioBytes)
        val entity = saveToVault(title, senderName, senderNumber, durationSeconds, tempFile, notes)
        tempFile.delete()
        entity
    }

    suspend fun deleteVaultItem(item: VaultItemEntity) = withContext(Dispatchers.IO) {
        cryptoManager.securelyDeleteFile(item.encryptedFileName)
        vaultDao.deleteItem(item)
    }

    suspend fun clearVault() = withContext(Dispatchers.IO) {
        vaultDao.clearVault()
        cryptoManager.clearTempDecryptedFiles()
    }

    suspend fun markAsRead(id: String) = withContext(Dispatchers.IO) {
        vaultDao.markAsRead(id)
    }
}
