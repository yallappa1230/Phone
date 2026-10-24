package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.local.FavoriteEntity
import com.example.data.security.CryptoManager
import com.example.ui.keypad.KeypadViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataStoreManager = DataStoreManager(context)
        cryptoManager = CryptoManager(context)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testAppNameStringResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Phone", appName)
    }

    @Test
    fun testFavoriteDaoPersistence() = runBlocking {
        val dao = db.favoriteDao()
        val fav = FavoriteEntity(
            contactId = "c100",
            displayName = "Steve Wozniak",
            phoneNumber = "+14085551212"
        )
        dao.addFavorite(fav)

        val list = dao.getAllFavorites().first()
        assertEquals(1, list.size)
        assertEquals("Steve Wozniak", list[0].displayName)

        val isFav = dao.isFavoriteSync("c100")
        assertTrue(isFav)

        dao.removeFavoriteById("c100")
        val isFavAfter = dao.isFavoriteSync("c100")
        assertFalse(isFavAfter)
    }

    @Test
    fun testPinHashingAndVerification() {
        val pin = "1234"
        val salt = cryptoManager.generateSalt()
        val hash = cryptoManager.hashPin(pin, salt)

        assertTrue(cryptoManager.verifyPin("1234", hash, salt))
        assertFalse(cryptoManager.verifyPin("0000", hash, salt))
        assertFalse(cryptoManager.verifyPin("1235", hash, salt))
    }

    @Test
    fun testKeypadViewModelEntryAndBackspace() {
        val vm = KeypadViewModel(context, dataStoreManager)
        assertEquals("", vm.uiState.value.enteredNumber)

        vm.onDigitClick("9")
        vm.onDigitClick("8")
        vm.onDigitClick("7")
        assertEquals("987", vm.uiState.value.enteredNumber)

        vm.onZeroLongClick()
        assertEquals("987+", vm.uiState.value.enteredNumber)

        vm.onBackspaceClick()
        assertEquals("987", vm.uiState.value.enteredNumber)

        vm.onBackspaceLongClick()
        assertEquals("", vm.uiState.value.enteredNumber)
    }

    @Test
    fun testDataStoreSettings() = runBlocking {
        dataStoreManager.setThemeMode("DARK")
        assertEquals("DARK", dataStoreManager.themeModeFlow.first())

        dataStoreManager.setVoicemailSim1("52121")
        assertEquals("52121", dataStoreManager.voicemailSim1Flow.first())
    }
}
