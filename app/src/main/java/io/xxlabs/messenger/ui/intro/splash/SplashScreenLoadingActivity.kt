package io.xxlabs.messenger.ui.intro.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.dialog.PopupActionDialog
import io.xxlabs.messenger.support.extensions.getTransition
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.ui.base.BaseKeystoreActivity
import io.xxlabs.messenger.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_splash.*
import timber.log.Timber
import java.util.*
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

    private fun loadAndGenerateKeys() {
        splashScreenViewModel.onRegisterSession.observe(this, { registered ->
            if (registered) {
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateNext()
                }, 1000)
            } else {
                createKeysGenerationErrorPopup()
            }
        })

        deletePreviousKeys()
        if (checkGenerateKeys()) {
            if (isHardwareBackedKeyStore()) {
                Timber.v("OS is hardware-backed")
                generatePassword {
                    registerUser()
                }
            } else {
                Timber.e("OS is not hardware-backed, showing popup")
                if (isMockVersion()) {
                    generatePassword {
                        registerUser()
                    }
                } else {
                    createEnvironmentErrorPopup()
                }
            }
        } else {
            Timber.e("Could not generate keys")
            createKeysGenerationErrorPopup()
        }
    }

    override fun onStart() {
        super.onStart()
        loadAndGenerateKeys()
    }

    private fun navigateNext() {
        val activity = Intent(
            this@SplashScreenLoadingActivity,
            MainActivity::class.java
        )

        val options = getTransition(R.anim.fade_in, R.anim.fade_out)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this)
    }

    private fun registerUser() {
        try {
            splashScreenViewModel.registerUser(this, rsaDecryptPwd())
        } catch (err: Exception) {
            showError(err.localizedMessage ?: "Error, could not create keys, try again.")
        }
    }

    fun createKeysGenerationErrorPopup() {
        PopupActionDialog.getInstance(
            this,
            icon = R.drawable.ic_alert_rounded,
            titleText = "Error",
            subtitleText = "An error occurred while trying to generate your keys. Please, try again.",
            positiveBtnText = "Ok",
            onClickPositive = {
                finishAndRemoveTask()
            }
        ).show()
    }

    fun createEnvironmentErrorPopup() {
        PopupActionDialog.getInstance(
            this,
            icon = R.drawable.ic_alert_rounded,
            titleText = "Error",
            subtitleText = "Your hardware is not a trusted execution environment. In order fully generate and store hardware-backed keys, the system must have a trusted execution environment in a system on a chip (SoC). Without this, we cannot fully guarantee your generated credentials will be stored in a safe manner.",
            positiveBtnText = "Proceed Anyways",
            onClickPositive = {
                generatePassword {
                    registerUser()
                }
            },
            negativeBtnText = "Do not generate",
            onClickNegative = {
                finishAndRemoveTask()
            },
        ).show()
    }
}