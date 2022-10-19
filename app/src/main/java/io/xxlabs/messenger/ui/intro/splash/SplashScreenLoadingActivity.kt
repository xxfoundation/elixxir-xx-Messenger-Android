package io.xxlabs.messenger.ui.intro.splash

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.extensions.getTransition
import io.xxlabs.messenger.ui.base.BaseKeystoreActivity
import io.xxlabs.messenger.ui.intro.registration.RegistrationFlowActivity
import javax.inject.Inject

class SplashScreenLoadingActivity : BaseKeystoreActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var splashScreenViewModel: SplashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()
        setContentView(R.layout.activity_splash)
        splashScreenViewModel =
            ViewModelProvider(this, viewModelFactory).get(SplashScreenViewModel::class.java)

        splashScreenViewModel.clearAppData()
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

    override fun onStart() {
        super.onStart()
        observeState()
    }

    private fun observeState() {
        splashScreenViewModel.appDataCleared.observe(this) { isReady ->
            if (isReady) navigateNext()
        }
    }

    private fun navigateNext() {
        val activity = Intent(
            this@SplashScreenLoadingActivity,
            RegistrationFlowActivity::class.java
        )

        val options = getTransition(R.anim.fade_in, R.anim.fade_out)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this)
    }
}