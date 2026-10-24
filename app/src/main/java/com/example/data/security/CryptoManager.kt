package com.example.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "PrivateVoicemailMasterKey"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val GCM_IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val PBKDF2_ITERATIONS = 10000
        private const val KEY_LENGTH = 256
    }

    private var isFallbackMode = false
    private var fallbackKey: SecretKey? = null

    private val keyStore: KeyStore? = try {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    } catch (_: Exception) {
        isFallbackMode = true
        null
    }

    init {
        ensureKeyExists()
    }

    private fun ensureKeyExists() {
        if (isFallbackMode || keyStore == null) {
            if (fallbackKey == null) {
                val keyGen = KeyGenerator.getInstance("AES")
                keyGen.init(KEY_LENGTH)
                fallbackKey = keyGen.generateKey()
            }
            return
        }

        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(ALGORITHM, ANDROID_KEYSTORE)
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setKeySize(KEY_LENGTH)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {
            isFallbackMode = true
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(KEY_LENGTH)
            fallbackKey = keyGen.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        if (isFallbackMode || keyStore == null) {
            return fallbackKey ?: run {
                val keyGen = KeyGenerator.getInstance("AES")
                keyGen.init(KEY_LENGTH)
                keyGen.generateKey().also { fallbackKey = it }
            }
        }
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    /**
     * Encrypts plaintext bytes using AES-GCM-256 with an Android Keystore key.
     * Returns Pair<encryptedBytes, ivBase64>.
     */
    fun encryptData(plainBytes: ByteArray): Pair<ByteArray, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainBytes)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        return Pair(encrypted, ivBase64)
    }

    /**
     * Decrypts ciphertext bytes using AES-GCM-256 with the stored IV.
     */
    fun decryptData(encryptedBytes: ByteArray, ivBase64: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher.doFinal(encryptedBytes)
    }

    /**
     * Encrypts an audio file from source into the app's secure private directory.
     * Returns Pair<encryptedFileName, ivBase64>.
     */
    fun encryptFile(sourceFile: File, destFileName: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

        val vaultDir = File(context.filesDir, "vault_encrypted").apply {
            if (!exists()) mkdirs()
        }
        val destFile = File(vaultDir, destFileName)

        FileInputStream(sourceFile).use { input ->
            CipherOutputStream(FileOutputStream(destFile), cipher).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
            }
        }

        return Pair(destFileName, ivBase64)
    }

    /**
     * Decrypts an encrypted file to a temporary playback file.
     */
    fun decryptFileToTemp(encryptedFileName: String, ivBase64: String): File {
        val vaultDir = File(context.filesDir, "vault_encrypted")
        val encryptedFile = File(vaultDir, encryptedFileName)
        val tempDir = File(context.cacheDir, "vault_temp").apply {
            if (!exists()) mkdirs()
        }
        val tempDecryptedFile = File(tempDir, "temp_${System.currentTimeMillis()}.m4a")

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        FileInputStream(encryptedFile).use { input ->
            CipherInputStream(input, cipher).use { cipherIn ->
                FileOutputStream(tempDecryptedFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (cipherIn.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
        }

        return tempDecryptedFile
    }

    /**
     * Securely wipes and deletes a file by overwriting with zeroes.
     */
    fun securelyDeleteFile(fileName: String) {
        val vaultDir = File(context.filesDir, "vault_encrypted")
        val file = File(vaultDir, fileName)
        if (file.exists()) {
            try {
                val length = file.length()
                if (length > 0) {
                    FileOutputStream(file).use { out ->
                        val zeroes = ByteArray(4096)
                        var written: Long = 0
                        while (written < length) {
                            val toWrite = minOf(zeroes.size.toLong(), length - written).toInt()
                            out.write(zeroes, 0, toWrite)
                            written += toWrite
                        }
                    }
                }
            } catch (_: Exception) {}
            file.delete()
        }
    }

    /**
     * Cleans temporary playback cache files.
     */
    fun clearTempDecryptedFiles() {
        val tempDir = File(context.cacheDir, "vault_temp")
        if (tempDir.exists()) {
            tempDir.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }

    // --- PIN Hashing & Verification with PBKDF2 ---

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashPin(pin: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun verifyPin(enteredPin: String, storedHash: String, storedSalt: String): Boolean {
        val computedHash = hashPin(enteredPin, storedSalt)
        // Constant-time comparison
        return slowEquals(
            Base64.decode(computedHash, Base64.NO_WRAP),
            Base64.decode(storedHash, Base64.NO_WRAP)
        )
    }

    private fun slowEquals(a: ByteArray, b: ByteArray): Boolean {
        var diff = a.size xor b.size
        val minLen = minOf(a.size, b.size)
        for (i in 0 until minLen) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }
}
