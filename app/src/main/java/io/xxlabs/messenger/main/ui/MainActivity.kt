package io.xxlabs.messenger.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import io.elixxir.core.ui.util.openLink
import io.elixxir.core.ui.view.SnackBarActivity
import io.xxlabs.messenger.databinding.ActivityMainBinding
import io.xxlabs.messenger.main.model.*
import io.xxlabs.messenger.main.window.WindowManager
import kotlinx.coroutines.launch

/**
 * The single Activity that hosts all Fragments.
 * Responsible for navigation between features and enforces minimum app version.
 */
class MainActivity : AppCompatActivity(), WindowManager, SnackBarActivity {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeState()
            }
        }

        intent?.let { handleIntent(it) }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        TODO("Navigate to requests screen")
    }

    private fun notificationIntent(intent: Intent) {
        TODO("Navigate to chat/group chat")
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
        TODO("Navigate to nav_registration graph")
    }

    private fun navigateToMain() {
        TODO("Navigate to nav_main graph")
    }

    private fun showAlert(alertUi: VersionAlertUi) {
        TODO()
    }

    override fun setFullScreen(fullScreen: Boolean) {
        if (fullScreen) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root)
            .show(WindowInsetsCompat.Type.systemBars())
    }

    override fun createSnackMessage(msg: String, forceMessage: Boolean): Snackbar {
        return Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).apply {
            view.translationZ = 10f
            show()
        }
    }

    companion object {
        const val INTENT_NOTIFICATION_CLICK = "nav_bundle"
        const val INTENT_PRIVATE_CHAT = "private_message"
        const val INTENT_GROUP_CHAT = "group_message"
        const val INTENT_REQUEST = "request"
        const val INTENT_INVITATION = "invitation"
    }
}