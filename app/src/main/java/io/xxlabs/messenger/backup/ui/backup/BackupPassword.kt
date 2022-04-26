package io.xxlabs.messenger.backup.ui.backup

import io.xxlabs.messenger.R


@JvmInline
value class BackupPassword(val value: String) {
    val isValid: Boolean get() = validationError == null
    val validationError: Int?
        get() {
            return when {
                tooShort() -> R.string.backup_password_too_short
                missingUppercase() -> R.string.backup_password_missing_uppercase
                missingLowercase() -> R.string.backup_password_missing_lowercase
                missingNumber() -> R.string.backup_password_missing_digit
                else -> null
            }
        }

    private fun tooShort() = value.length < MIN_LENGTH

    private fun missingUppercase(): Boolean {
        for (char in value) {
            if (char.isUpperCase()) return false
        }
        return true
    }
    private fun missingLowercase(): Boolean {
        for (char in value) {
            if (char.isLowerCase()) return false
        }
        return true
    }

    private fun missingNumber(): Boolean {
        for (char in value) {
            if (char.isDigit()) return false
        }
        return true
    }

    companion object {
        const val MIN_LENGTH = 8
        const val MAX_LENGTH = 64
    }
}