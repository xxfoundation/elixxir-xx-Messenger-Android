package io.xxlabs.messenger.ui.intro.registration

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.snackbar.Snackbar
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ActivityRegistrationFlowBinding
import io.xxlabs.messenger.support.extensions.getTransition
import io.xxlabs.messenger.support.view.SnackBarActivity
import io.xxlabs.messenger.ui.base.BaseKeystoreActivity
import io.xxlabs.messenger.ui.main.MainActivity

/**
 * Presents and handles navigation for registration and restore account UI.
 */
class RegistrationFlowActivity : BaseKeystoreActivity(), RegistrationHandler, SnackBarActivity {

    private lateinit var binding: ActivityRegistrationFlowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()
        binding = ActivityRegistrationFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

    override fun onRegistrationComplete() {
        val activity = Intent(
            this@RegistrationFlowActivity,
            MainActivity::class.java
        )

        val options = getTransition(R.anim.fade_in, R.anim.fade_out)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this)
    }

    override fun createSnackMessage(msg: String, forceMessage: Boolean): Snackbar {
        return Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).apply {
            view.translationZ = 10f
            show()
        }
    }
}

interface RegistrationHandler {
    fun onRegistrationComplete()
}