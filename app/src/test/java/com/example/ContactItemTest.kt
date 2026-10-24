package com.example

import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import com.example.data.model.CarrierVoicemailConfig
import com.example.data.model.ContactItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactItemTest {

    @Test
    fun testInitialsComputation() {
        assertEquals("JD", ContactItem.computeInitials("John Doe"))
        assertEquals("AL", ContactItem.computeInitials("Ada Lovelace"))
        assertEquals("A", ContactItem.computeInitials("Alice"))
        assertEquals("?", ContactItem.computeInitials(""))
        assertEquals("?", ContactItem.computeInitials("   "))
    }

    @Test
    fun testCallLogItemFormatting() {
        val missedCall = CallLogItem(
            id = "1",
            number = "+919876543210",
            callerName = "Airtel Customer",
            callType = CallType.MISSED,
            timestamp = System.currentTimeMillis(),
            durationSeconds = 0
        )
        assertTrue(missedCall.isMissed)
        assertEquals("Airtel Customer", missedCall.displayTitle)
        assertEquals("", missedCall.formattedDuration())

        val incomingCall = CallLogItem(
            id = "2",
            number = "+919876543210",
            callerName = null,
            callType = CallType.INCOMING,
            timestamp = System.currentTimeMillis() - 3600000,
            durationSeconds = 125
        )
        assertFalse(incomingCall.isMissed)
        assertEquals("+919876543210", incomingCall.displayTitle)
        assertEquals("2m 5s", incomingCall.formattedDuration())
    }

    @Test
    fun testCarrierVoicemailPresets() {
        val presets = CarrierVoicemailConfig.PRESETS
        assertTrue(presets.isNotEmpty())

        val jio = presets.first { it.carrierName.contains("Jio") }
        assertEquals("*86", jio.defaultAccessNumber)

        val airtel = presets.first { it.carrierName.contains("Airtel") }
        assertEquals("52121", airtel.defaultAccessNumber)
        assertEquals("*61*52121#", airtel.activationCode)
    }
}
