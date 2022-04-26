package io.xxlabs.messenger.backup.cloud

import android.content.Intent

/**
 * Handles authentication with a [BackupLocation] that requires sign-in.
 */
interface AuthHandler {
    val signInIntent: Intent
    fun handleSignInResult(data: Intent?)
    fun signOut()
}