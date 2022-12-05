package io.xxlabs.messenger.backup.cloud.dropbox

import android.content.Intent
import io.xxlabs.messenger.backup.cloud.AuthHandler
import io.xxlabs.messenger.backup.cloud.AuthResultCallback
import io.xxlabs.messenger.backup.cloud.dropbox.DropboxAuthActivity.Companion.EXTRA_DBX_CREDENTIAL
import io.xxlabs.messenger.backup.cloud.dropbox.DropboxAuthActivity.Companion.START_OAUTH_INTENT
import io.xxlabs.messenger.support.appContext
import timber.log.Timber

/**
 * Handles authenticating with Dropbox.
 */
class DropboxAuthHandler(
    private val callback: AuthResultCallback
) : AuthHandler {

    override val signInIntent: Intent
        get() = Intent(appContext(), DropboxAuthActivity::class.java).apply {
            action = START_OAUTH_INTENT
        }

    override fun handleSignInResult(data: Intent?) {
        data?.getStringExtra(EXTRA_DBX_CREDENTIAL)?.run {
            if (!isNullOrEmpty()) {
                return callback.onSuccess()
            }
        }

        callback.onFailure("Failed to login. Please try again.")
    }

    override fun signOut() {
        Timber.d("Dropbox sign out clicked")
    }
}