package com.example.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CallLogRepository(private val context: Context) {

    fun hasCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCallLogs(filterMissedOnly: Boolean = false): Flow<List<CallLogItem>> = flow {
        if (!hasCallLogPermission()) {
            emit(emptyList())
            return@flow
        }

        val callLogs = mutableListOf<CallLogItem>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        val selection = if (filterMissedOnly) {
            "${CallLog.Calls.TYPE} = ? OR ${CallLog.Calls.TYPE} = ?"
        } else {
            null
        }

        val selectionArgs = if (filterMissedOnly) {
            arrayOf(
                CallLog.Calls.MISSED_TYPE.toString(),
                CallLog.Calls.REJECTED_TYPE.toString()
            )
        } else {
            null
        }

        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT 100"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(CallLog.Calls._ID)
                val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getString(idIdx) ?: "" else ""
                    val number = if (numberIdx >= 0) it.getString(numberIdx) ?: "" else ""
                    val cachedName = if (nameIdx >= 0) it.getString(nameIdx) else null
                    val rawType = if (typeIdx >= 0) it.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE
                    val date = if (dateIdx >= 0) it.getLong(dateIdx) else 0L
                    val duration = if (durationIdx >= 0) it.getLong(durationIdx) else 0L

                    val callType = when (rawType) {
                        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        CallLog.Calls.VOICEMAIL_TYPE -> CallType.VOICEMAIL
                        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                        else -> CallType.UNKNOWN
                    }

                    callLogs.add(
                        CallLogItem(
                            id = id,
                            number = number,
                            callerName = cachedName,
                            callType = callType,
                            timestamp = date,
                            durationSeconds = duration
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            emit(emptyList())
            return@flow
        } catch (_: Exception) {
            // Handled gracefully
        }

        emit(callLogs)
    }.flowOn(Dispatchers.IO)
}
