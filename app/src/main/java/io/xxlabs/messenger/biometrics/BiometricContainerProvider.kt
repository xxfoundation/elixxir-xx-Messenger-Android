package io.xxlabs.messenger.biometrics

import android.content.Intent
import android.provider.Settings
import android.util.Base64
import androidx.fragment.app.Fragment
import bindings.Bindings
import io.xxlabs.messenger.R
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.dialog.BottomSheetPopup
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.extensions.toast

class BiometricContainerProvider(
    val containerFragment: Fragment,
    val preferences: PreferencesRepository,
    val biometricContainerCallback: BiometricContainerCallback
) : BiometricCallback {
    val context = containerFragment.requireContext()
    var currentBiometricPromptManager: BiometricPromptManager? = null

    //Biometrics
    fun isFingerprintEnabled(): Boolean {
        return preferences.isFingerprintEnabled
    }

    fun isKeySet(): Boolean {
        return preferences.userBiometricKey.isNotEmpty()
    }

    fun resetKey() {
        context.toast(R.string.biometric_reset)
        preferences.isFingerprintEnabled = false
        preferences.userBiometricKey = ""
    }

    override fun onBiometricAuthenticationNotSupported() {
        context.toast(R.string.biometric_error_hardware_not_supported)
        biometricContainerCallback.onErrorDo()
    }

    override fun onBiometricAuthenticationNotAvailable() {
        resetKey()
        biometricContainerCallback.onBiometricsNotAvailable()
    }

    override fun onBiometricAuthenticationPermissionNotGranted() {
        context.toast(R.string.biometric_error_permission_not_granted)
        biometricContainerCallback.onErrorDo()
    }

    override fun onBiometricFingerprintNotEnrolled(isDecryption: Boolean) {
        biometricContainerCallback.onBiometricFingerprintNotEnrolledDo(isDecryption)
    }

    override fun onBiometricAuthenticationInternalError(error: String?) {
        context.toast(error.toString())
        showBioSetupFail()
    }

    override fun onAuthenticationFailed() {
        biometricContainerCallback.onFailedDo()
    }

    override fun onAuthenticationCancelled() {
        biometricContainerCallback.onCancelDo()
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        context.toast(helpString.toString())
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        biometricContainerCallback.onErrorDo()
    }

    override fun onAuthenticationSucceeded(key: String?) {
        if (key.isNullOrEmpty()) {
            showBioSetupSuccess()
        } else {
            biometricContainerCallback.onSuccessDo()
        }
    }

    fun openBiometricsSetup(
        hideCheckbox: Boolean = false,
        primaryBtnCallback: () -> Unit = { },
        secondaryBtnCallback: () -> Unit = { },
        checkCallback: (Boolean) -> Unit = { },
        onCancel: () -> Unit = {}
    ) {
        if (hideCheckbox) {
            BottomSheetPopup.getInstance(
                context,
                description = "By enabling biometrics, you will be required to authenticate every time the app is reopened.",
                topButtonTitle = "Yes, enable",
                topButtonClick = primaryBtnCallback,
                topButtonDismiss = true,
                bottomButtonTitle = "Maybe later",
                bottomButtonClick = secondaryBtnCallback,
                bottomButtonDismiss = true,
                onCancel = onCancel
            ).show()
        } else {
            BottomSheetPopup.getInstance(
                context,
                description = "Would you like to login to this account using biometrics?",
                topButtonTitle = "Yes, enable",
                topButtonClick = primaryBtnCallback,
                topButtonDismiss = true,
                bottomButtonTitle = "Maybe later",
                bottomButtonClick = secondaryBtnCallback,
                bottomButtonDismiss = true,
                checkBoxTitle = "Don\'t show this message again.",
                checkBoxCallback = checkCallback,
                onCancel = onCancel
            ).show()
        }
    }

    fun checkFingerprintSetup() = if (isFingerprintEnabled() && isKeySet()) {
        authenticateViaFingerprint(true)
    } else {
        authenticateViaFingerprint(false)
    }

    fun authenticateViaFingerprint(isDecryptMode: Boolean) {
        if (isDecryptMode) {
            val bioManager = getBiometricManagerInstanceLogin(preferences.name)
            currentBiometricPromptManager = bioManager
            bioManager.authenticate(this)
        } else {
            val bioManager = getBiometricManagerInstanceRegister(preferences.name)
            currentBiometricPromptManager = bioManager
            bioManager.authenticate(
                this,
                Bindings.generateSecret(32).toBase64String(Base64.DEFAULT)
            )
        }
    }

    private fun showBioSetupSuccess() {
        preferences.isFingerprintEnabled = true
        BottomSheetPopup.getInstance(
            context,
            icon = R.drawable.ic_check_circle,
            description = "Your biometrics were successfully registered!",
            topButtonTitle = "Ok",
            topButtonClick = { biometricContainerCallback.onSuccessDo() },
            topButtonDismiss = true,
            onCancel = { biometricContainerCallback.onSuccessDo() }
        ).show()
    }

    private fun showBioSetupFail() {
        preferences.isFingerprintEnabled = false
        BottomSheetPopup.getInstance(
            context,
            icon = R.drawable.ic_alert_rounded,
            description = "Failed to authenticate biometrics",
            topButtonTitle = "Ok",
            topButtonClick = { biometricContainerCallback.onErrorDo() },
            topButtonDismiss = true,
            onCancel = { biometricContainerCallback.onErrorDo() }
        ).show()
    }

    private fun getBiometricManagerInstanceLogin(username: String): BiometricPromptManager {
        return BiometricPromptManager.BiometricBuilder(containerFragment, preferences)
            .setTitle("Sign in")
            .setSubtitle(username)
            .setDescription("Place your finger on the device home button to verify your identity")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun getBiometricManagerInstanceRegister(username: String): BiometricPromptManager {
        return BiometricPromptManager.BiometricBuilder(containerFragment, preferences)
            .setTitle("Register Biometrics")
            .setSubtitle(username)
            .setDescription("Place your finger on the device home button to verify your identity")
            .setNegativeButtonText("Cancel")
            .build()
    }

    fun showEnableBiometrics() {
        BottomSheetPopup.getInstance(
            context,
            icon = R.drawable.ic_alert_rounded,
            "Your are required to enroll to a secure lock and biometrics before activating this option.",
            topButtonTitle = "Open Security Settings",
            topButtonClick = {
                openSecuritySettings()
                biometricContainerCallback.onCancelDo()
            }, bottomButtonTitle = "Maybe Later",
            bottomButtonClick = {
                biometricContainerCallback.onCancelDo()
            },
            topButtonDismiss = true,
            bottomButtonDismiss = true,
            onCancel = { biometricContainerCallback.onCancelDo() }
        ).show()
    }

    private fun openSecuritySettings() {
        val settingsIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        containerFragment.startActivity(settingsIntent)
    }

    companion object {
        fun getInstance(
            containerFragment: Fragment,
            preferences: PreferencesRepository,
            biometricContainerCallback: BiometricContainerCallback
        ): BiometricContainerProvider {
            return BiometricContainerProvider(
                containerFragment,
                preferences,
                biometricContainerCallback
            )
        }
    }
}