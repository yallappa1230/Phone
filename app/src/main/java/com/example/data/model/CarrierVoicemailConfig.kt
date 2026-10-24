package com.example.data.model

data class CarrierVoicemailConfig(
    val carrierName: String,
    val defaultAccessNumber: String,
    val activationCode: String,
    val deactivationCode: String,
    val setupInstructions: String,
    val notes: String
) {
    companion object {
        val PRESETS = listOf(
            CarrierVoicemailConfig(
                carrierName = "Reliance Jio",
                defaultAccessNumber = "*86",
                activationCode = "*401*<voicemail_number>",
                deactivationCode = "*402",
                setupInstructions = "Jio provides conditional call forwarding and JioCall integration. To route unanswered calls to Jio Voicemail or custom mailbox, activate conditional forwarding using *401* (or *409* for unreachable, *403* when busy).",
                notes = "Requires active Jio subscription and carrier voicemail service provisioned on your circle."
            ),
            CarrierVoicemailConfig(
                carrierName = "Bharti Airtel",
                defaultAccessNumber = "52121",
                activationCode = "*61*52121#",
                deactivationCode = "##61#",
                setupInstructions = "Airtel Voicemail service operates on short code 52121. Activate conditional forwarding on no-reply via *61*52121# (or 30-sec ring timeout via *61*52121**30#). Call 52121 to record your personal greeting and retrieve messages.",
                notes = "Carrier charges may apply depending on your Airtel postpaid/prepaid tariff plan."
            ),
            CarrierVoicemailConfig(
                carrierName = "Vodafone Idea (Vi)",
                defaultAccessNumber = "198",
                activationCode = "*61*198#",
                deactivationCode = "##61#",
                setupInstructions = "Vi voicemail can be configured via conditional call forwarding *61*198# or through the Vi customer care self-service menu.",
                notes = "Available in select telecom circles."
            ),
            CarrierVoicemailConfig(
                carrierName = "Standard / US Carriers",
                defaultAccessNumber = "*86",
                activationCode = "*86",
                deactivationCode = "N/A",
                setupInstructions = "Standard cellular carriers (Verizon *86, AT&T 1-long-press, T-Mobile 123) use automated carrier switches to manage voicemails on their network towers.",
                notes = "Hold 1 or dial *86 to connect directly."
            )
        )
    }
}
