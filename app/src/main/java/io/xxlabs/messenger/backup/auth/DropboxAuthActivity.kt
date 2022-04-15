package io.xxlabs.messenger.backup.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.backup.data.Dropbox.Companion.CLIENT_IDENTIFIER

/**
 * Dropbox Auth activity wrapper
 */
class DropboxAuthActivity : Activity() {

    private var shownLoginPrompt = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == START_OAUTH_INTENT) {
            launchDropboxSignIn()
        }
    }

    private fun launchDropboxSignIn() {
        val config = DbxRequestConfig.newBuilder(CLIENT_IDENTIFIER).build()
        Auth.startOAuth2PKCE(this, APP_KEY, config, oauthScopes)
    }

    override fun onResume() {
        super.onResume()
        if (shownLoginPrompt) {
            val intent = Intent(START_OAUTH_INTENT).apply {
                putExtra(EXTRA_DBX_CREDENTIAL, Auth.getDbxCredential()?.toString())
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onPause() {
        shownLoginPrompt = true
        super.onPause()
    }

    companion object  {
        const val START_OAUTH_INTENT = "start_oauth"
        const val EXTRA_DBX_CREDENTIAL = "dbx_credential"

        private var APP_KEY = BuildConfig.DROPBOX_KEY
        private const val ACCT_INFO_SCOPE = "account_info.read"
        private const val READ_FILES_SCOPE = "files.content.read"
        private const val READ_METADATA_SCOPE = "files.metadata.read"
        private const val WRITE_FILES_SCOPE = "files.content.write"
        private val oauthScopes = listOf(
            ACCT_INFO_SCOPE,
            READ_FILES_SCOPE,
            READ_METADATA_SCOPE,
            WRITE_FILES_SCOPE
        )
    }
}