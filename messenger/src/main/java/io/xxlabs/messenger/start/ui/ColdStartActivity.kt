package io.xxlabs.messenger.start.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.xxlabs.messenger.MainActivity
import io.xxlabs.messenger.MainActivity.Companion.INTENT_INVITATION
import io.xxlabs.messenger.MainActivity.Companion.INTENT_NOTIFICATION_CLICK
import io.xxlabs.messenger.R
import io.xxlabs.messenger.registration.RegistrationStartActivity
import io.xxlabs.messenger.start.model.AppVersionStatus.*
import io.xxlabs.messenger.util.getTransition

/**
 * The app entry point when initially launched. Has no UI.
 * Handles Intent (if applicable) and routes to
 * Onboarding (new user) or MainActivity (existing user).
 */
class ColdStartActivity : AppCompatActivity() {

    private val viewModel: ColdStartViewModel by viewModels()
    private var mainIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        intent?.let { handleIntent(it) }
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
                this@ColdStartActivity,
                MainActivity::class.java
            ).apply {
                putExtra(INTENT_NOTIFICATION_CLICK, it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        observeUi()
    }

    private fun observeUi() {
        viewModel.navigateToRegistration.observe(this) { go ->
            if (go) navigateToRegistration()
        }

        viewModel.navigateToMain.observe(this) { go ->
            if (go) navigateToMain()
        }

        viewModel.versionAlert.observe(this) { alert ->
            alert?.let {
                showAlert(it)
                viewModel.onVersionAlertShown()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                showError(error)
            }
        }
    }

    private fun navigateToRegistration() {
        val activity = Intent(
            this@ColdStartActivity,
            RegistrationStartActivity::class.java
        )

        val options = getTransition(R.anim.hold, R.anim.hold)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this)
    }

    private fun navigateToMain() {
        val activity = mainIntent ?: Intent(
            this@ColdStartActivity,
            MainActivity::class.java
        )

        val options = getTransition(R.anim.fade_in, R.anim.fade_out)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this@ColdStartActivity)
    }

    private fun showAlert(alertUi: VersionAlertUi) {

    }

    private fun showError(error: String) {

    }

    fun showUpdateRecommended(downloadUrl: String) {
        PopupActionDialog.getInstance(
            this,
            icon = R.drawable.ic_alert_rounded,
            titleText = "App Update",
            subtitleText = "There is a new version available that enhance the current performance and usability.",
            positiveBtnText = "Update",
            onClickPositive = {
                Utils.openLink(this, downloadUrl)
            },
            negativeBtnText = "Not now",
            onClickNegative = {
                navigateMain()
            },
            onDismissOnly = {
                navigateMain()
            },
            isCancellable = true
        ).show()
    }

    fun showUpdateRequired(downloadUrl: String, popupMessage: String) {
        PopupActionDialog.getInstance(
            this,
            icon = R.drawable.ic_alert_rounded,
            titleText = "App Update",
            subtitleText = popupMessage,
            positiveBtnText = "Okay",
            onClickPositive = {
                Utils.openLink(this, downloadUrl)
            }
        ).show()
    }
}