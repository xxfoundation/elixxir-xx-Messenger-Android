package io.xxlabs.messenger.biometrics

interface BiometricContainerCallback {
    fun onBiometricsNotAvailable()
    fun onBiometricFingerprintNotEnrolledDo(isDecryptionMode: Boolean)
    fun onFailedDo()
    fun onSuccessDo()
    fun onErrorDo()
    fun onCancelDo()
}