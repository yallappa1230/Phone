package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    VOICEMAIL,
    REJECTED,
    BLOCKED,
    UNKNOWN
}

data class CallLogItem(
    val id: String,
    val number: String,
    val callerName: String?,
    val callType: CallType,
    val timestamp: Long,
    val durationSeconds: Long
) {
    val displayTitle: String
        get() = callerName?.takeIf { it.isNotBlank() } ?: number.takeIf { it.isNotBlank() } ?: "Unknown Caller"

    val isMissed: Boolean
        get() = callType == CallType.MISSED || callType == CallType.REJECTED

    fun formattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val oneDay = 24 * 60 * 60 * 1000L

        return when {
            diff < oneDay -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                timeFormat.format(Date(timestamp))
            }
            diff < 2 * oneDay -> "Yesterday"
            diff < 7 * oneDay -> {
                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                dayFormat.format(Date(timestamp))
            }
            else -> {
                val dateFormat = SimpleDateFormat("M/d/yy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }

    fun formattedDuration(): String {
        if (durationSeconds <= 0) return ""
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
    }
}
