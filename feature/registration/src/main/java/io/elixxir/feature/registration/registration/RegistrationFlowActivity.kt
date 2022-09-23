package io.elixxir.feature.registration.registration

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import io.elixxir.core.ui.util.getTransition
import io.elixxir.core.ui.view.SnackBarActivity
import io.elixxir.feature.registration.R
import io.elixxir.feature.registration.databinding.ActivityRegistrationFlowBinding


/**
 * Presents and handles navigation for registration and restore account UI.
 */
class RegistrationFlowActivity : AppCompatActivity(), SnackBarActivity {

    private lateinit var binding: ActivityRegistrationFlowBinding
    private val viewModel: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenResumed {
            observeEvents()
        }

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

    private suspend fun observeEvents() {
        viewModel.registrationComplete.collect { complete ->
            if (complete) onRegistrationComplete()
        }
    }

    private fun onRegistrationComplete() {
        // TODO: Explicit deep link to MainActivity in :feature:home module
//        val activity = Intent(
//            this@RegistrationFlowActivity,
//            MainActivity::class.java
//        )
//
//        val options = getTransition(R.anim.fade_in, R.anim.fade_out)
//        startActivity(activity, options)
//        ActivityCompat.finishAfterTransition(this)
    }

    override fun createSnackMessage(msg: String, forceMessage: Boolean): Snackbar {
        return Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).apply {
            view.translationZ = 10f
            show()
        }
    }
}