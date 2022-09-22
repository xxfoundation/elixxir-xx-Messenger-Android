package io.xxlabs.messenger.biometrics

interface BiometricCallback {
    fun onBiometricAuthenticationNotSupported()
    fun onBiometricAuthenticationNotAvailable()
    fun onBiometricAuthenticationPermissionNotGranted()
    fun onBiometricFingerprintNotEnrolled(isDecryption: Boolean)
    fun onBiometricAuthenticationInternalError(error: String?)
    fun onAuthenticationFailed()
    fun onAuthenticationCancelled()
    fun onAuthenticationSucceeded(key: String? = null)
    fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?)
    fun onAuthenticationError(errorCode: Int, errString: CharSequence?)
}