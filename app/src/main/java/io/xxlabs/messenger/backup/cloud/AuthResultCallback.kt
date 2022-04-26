package io.xxlabs.messenger.backup.cloud

/**
 * Exposes the result of an [AuthHandler] authentication attempt.
 */
interface AuthResultCallback {
    fun onFailure(errorMsg: String)
    fun onSuccess()
}