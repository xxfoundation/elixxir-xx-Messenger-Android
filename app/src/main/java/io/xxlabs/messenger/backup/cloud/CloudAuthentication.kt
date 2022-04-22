package io.xxlabs.messenger.backup.cloud

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Encapsulates authentication-related ActivityResultLaunchers
 * for an Activity or Fragment with the provided [registry].
 */
class CloudAuthentication(
    private val registry: ActivityResultRegistry
) : DefaultLifecycleObserver {

    private lateinit var launchSignIn: ActivityResultLauncher<AuthHandler>
    private var _handler: AuthHandler? = null

    private val signInContract: ActivityResultContract<AuthHandler, Intent?>
        get() {
            return object : ActivityResultContract<AuthHandler, Intent?>() {
                override fun createIntent(context: Context, input: AuthHandler) =
                    input.signInIntent

                override fun parseResult(resultCode: Int, result: Intent?): Intent? {
                    return result
                }
            }
        }

    private val signInCallback = ActivityResultCallback<Intent?> {
        _handler?.handleSignInResult(it)
    }

    override fun onCreate(owner: LifecycleOwner) {
        launchSignIn = registry.register(
            "cloudSignIn", owner, signInContract, signInCallback
        )
    }

    fun signIn(handler: AuthHandler) {
        _handler = handler
        launchSignIn.launch(handler)
    }
}