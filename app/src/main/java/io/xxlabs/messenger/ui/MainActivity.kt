package io.xxlabs.messenger.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.elixxir.core.ui.view.SnackBarActivity
import io.xxlabs.messenger.databinding.ActivityMainBinding

/**
 * The single Activity that hosts all Fragments.
 * Responsible for navigation between features and enforces minimum app version.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), WindowManager, SnackBarActivity {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
//        if (!viewModel.userExists()) return

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
        private const val INTENT_NOTIFICATION_CLICK = "nav_bundle"
        private const val INTENT_PRIVATE_CHAT = "private_message"
        private const val INTENT_GROUP_CHAT = "group_message"
        private const val INTENT_REQUEST = "request"
        private const val INTENT_INVITATION = "invitation"
    }
}