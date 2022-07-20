package io.xxlabs.messenger.ui.intro.splash

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import bindings.Bindings
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.RegistrationJsonWrapper
import io.xxlabs.messenger.support.dialog.PopupActionDialog
import io.xxlabs.messenger.support.extensions.getTransition
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.ui.base.BaseInjectorActivity
import io.xxlabs.messenger.ui.main.MainActivity
import io.xxlabs.messenger.ui.main.MainActivity.Companion.INTENT_DEEP_LINK_BUNDLE
import javax.inject.Inject

class SplashScreenPlaceholderActivity : BaseInjectorActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var splashScreenViewModel: SplashScreenViewModel
    private var mainIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()
        splashScreenViewModel =
            ViewModelProvider(this, viewModelFactory).get(SplashScreenViewModel::class.java)

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
        // Pass this intent on to MainActivity.
        mainIntent = intent.getBundleExtra(INTENT_DEEP_LINK_BUNDLE)?.let {
            Intent(
                this@SplashScreenPlaceholderActivity,
                MainActivity::class.java
            ).apply {
                putExtra(INTENT_DEEP_LINK_BUNDLE, it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        observeUi()
    }

    private fun observeUi() {
        splashScreenViewModel.navigateNext.observe(this) { navigate ->
            if (navigate) navigateNext()
        }

        splashScreenViewModel.appDataCleared.observe(this) { cleared ->
            if (cleared) newUserSession()
        }
    }

    private fun newUserSession() {
        val activity = Intent(
            this@SplashScreenPlaceholderActivity,
            RegistrationIntroActivity::class.java
        )

        val options = getTransition(R.anim.hold, R.anim.hold)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this)
    }

    private fun navigateNext() {
        try {
            if (isMockVersion()) {
                navigateMain()
                return
            }

            val jsonObject = downloadRegistrationJson()
            val registrationWrapper = RegistrationJsonWrapper.from(jsonObject)
            val appVersion = registrationWrapper.appVersion
            val minVersion = registrationWrapper.minVersion
            val recommendedVersion = registrationWrapper.recommendedVersion
            val downloadUrl = registrationWrapper.downloadUrl
            val popupMessage = registrationWrapper.minPopupMessage

            when {
                appVersion < minVersion -> {
                    createForcedUpdatePopup(downloadUrl, popupMessage)
                }
                appVersion >= minVersion && appVersion < recommendedVersion -> {
                    createRecommendedUpdatePopup(downloadUrl)
                }
                else -> {
                    navigateMain()
                }
            }
        } catch (err: Exception) {
            showError(err)
        }
    }

    private fun downloadRegistrationJson(): JsonObject {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val db = Bindings.downloadDAppRegistrationDB().decodeToString().replace("\n", "")
        return gson.fromJson(db, JsonObject::class.java)
    }

    private fun navigateMain() {
        val activity = mainIntent ?: Intent(
            this@SplashScreenPlaceholderActivity,
            MainActivity::class.java
        )

        val options = getTransition(R.anim.fade_in, R.anim.fade_out)
        startActivity(activity, options)
        ActivityCompat.finishAfterTransition(this@SplashScreenPlaceholderActivity)
    }

    fun createForcedUpdatePopup(downloadUrl: String, popupMessage: String) {
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

    fun createRecommendedUpdatePopup(downloadUrl: String) {
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
}