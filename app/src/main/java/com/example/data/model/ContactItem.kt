package com.example.data.model

data class ContactItem(
    val id: String,
    val lookupKey: String,
    val displayName: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val initials: String = computeInitials(displayName),
    val isFavorite: Boolean = false
) {
    companion object {
        fun computeInitials(name: String): String {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return "?"
            val parts = trimmed.split("\\s+".toRegex())
            return when {
                parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
                else -> trimmed.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            }
        }
    }
}
