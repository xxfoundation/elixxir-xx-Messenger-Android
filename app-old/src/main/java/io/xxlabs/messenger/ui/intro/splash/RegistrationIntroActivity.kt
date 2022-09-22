package io.xxlabs.messenger.ui.intro.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ActivityRegistrationIntroBinding
import io.xxlabs.messenger.support.extensions.getTransition

class RegistrationIntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()
        binding = ActivityRegistrationIntroBinding.inflate(layoutInflater)
        binding.registrationIntroNextButton.setOnClickListener { navigateToOnboarding() }
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

    private fun navigateToOnboarding() {
        val activity = Intent(
            this,
            SplashScreenLoadingActivity::class.java
        )

        val options = getTransition(R.anim.hold, R.anim.hold)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this)
    }
}