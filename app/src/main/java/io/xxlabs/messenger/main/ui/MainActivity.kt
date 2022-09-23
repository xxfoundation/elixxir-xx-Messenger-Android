package io.xxlabs.messenger.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.xxlabs.messenger.R
import io.elixxir.feature.registration.registration.RegistrationFlowActivity
import io.elixxir.core.ui.util.getTransition
import io.elixxir.core.ui.util.openLink
import io.xxlabs.messenger.main.model.*
import kotlinx.coroutines.launch

/**
 * The single Activity that hosts all Fragments.
 * Responsible for navigation between features and enforces minimum app version.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var mainIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeState()
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        intent?.let { handleIntent(it) }
        setContentView(R.layout.activity_main)
    }

    private fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // Invitations can only be handled if the user has an account.
        // Only a valid user can receive notifications.
        if (!viewModel.userExists()) return

        if (Intent.ACTION_VIEW == intent.action) {
            // Implicit Intent from an invitation link
            intent.data?.getQueryParameter("username")?.let { username ->
                invitationIntent(username)
            }
        } else notificationIntent(intent)
    }

    private fun invitationIntent(username: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(INTENT_INVITATION, username)
        }
        startActivity(intent)
        finish()
    }

    private fun notificationIntent(intent: Intent) {
        mainIntent = intent.getBundleExtra(INTENT_NOTIFICATION_CLICK)?.let {
            Intent(
                this@MainActivity,
                MainActivity::class.java
            ).apply {
                putExtra(INTENT_NOTIFICATION_CLICK, it)
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.appState.collect {
                with(it) {
                    when (versionState) {
                        is VersionOk -> {
                            if (userState == UserState.NewUser) navigateToRegistration()
                            else navigateToMain()
                        }
                        is UpdateRecommended -> showAlert(versionState.alertUi)
                        is UpdateRequired -> showAlert(versionState.alertUi)
                        else -> {}
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.launchUrl.collect {
                it?.let {
                    openLink(it)
                }
            }
        }
    }

    private fun navigateToRegistration() {
        val activity = Intent(
            this@MainActivity,
            RegistrationFlowActivity::class.java
        )

        val options = getTransition(R.anim.hold, R.anim.hold)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this)
    }

    private fun navigateToMain() {
        val activity = mainIntent ?: Intent(
            this@MainActivity,
            MainActivity::class.java
        )

        val options = getTransition(R.anim.fade_in, R.anim.fade_out)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this@MainActivity)
    }

    private fun showAlert(alertUi: VersionAlertUi) {
        TODO()
    }

    companion object {
        const val INTENT_NOTIFICATION_CLICK = "nav_bundle"
        const val INTENT_PRIVATE_CHAT = "private_message"
        const val INTENT_GROUP_CHAT = "group_message"
        const val INTENT_REQUEST = "request"
        const val INTENT_INVITATION = "invitation"
    }
}