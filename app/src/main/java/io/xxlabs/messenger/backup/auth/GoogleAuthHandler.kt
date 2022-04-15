package io.xxlabs.messenger.backup.auth

import android.content.Intent
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import io.xxlabs.messenger.backup.model.AuthHandler
import io.xxlabs.messenger.backup.model.AuthResultCallback
import io.xxlabs.messenger.support.appContext
import timber.log.Timber

/**
 * Handles authenticating with Google Sign In.
 */
class GoogleAuthHandler(
    private val callback: AuthResultCallback
) : AuthHandler {

    private val googleSignInClient: GoogleSignInClient by lazy {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(oAuthScopes)
            .build()
        GoogleSignIn.getClient(appContext(), signInOptions)
    }

    override val signInIntent: Intent
        get() {
            return googleSignInClient.signInIntent
        }

    private val genericErrorMessage: String = "An error occurred. Please try again."

    private fun handleAuthError(e: Exception?) {
        val message = (e as? ApiException)?.statusCode?.let {
            GoogleSignInStatusCodes.getStatusCodeString(it)
        } ?: e?.localizedMessage
        Timber.e(e)
        callback.onFailure(message ?: genericErrorMessage)
    }

    override fun handleSignInResult(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result.run {
                        Timber.d("$account : $displayName : $email")
                    }
                    callback.onSuccess()
                } else handleAuthError(it.exception)
            }
    }

    fun hasValidCredentials(): Boolean {
        GoogleSignIn.getLastSignedInAccount(appContext())?.run {
            when {
                requestedScopes != grantedScopes -> handleMissingScope(this)
                isExpired -> handleExpired(this)
                else -> return true
            }
        }
        signOut()
        return false
    }

    private fun handleMissingScope(account: GoogleSignInAccount) {
        account.requestExtraScopes()
    }

    private fun handleExpired(account: GoogleSignInAccount){
        account.requestExtraScopes(oAuthScopes)
    }

    override fun signOut() {
        googleSignInClient.signOut()
    }

    companion object {
        private val oAuthScopes = Scope(DriveScopes.DRIVE_APPDATA)
    }
}