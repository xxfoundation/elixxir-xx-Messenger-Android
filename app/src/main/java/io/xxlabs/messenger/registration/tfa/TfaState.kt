package io.xxlabs.messenger.ui.intro.registration.tfa

import android.text.Spanned
import androidx.lifecycle.LiveData

interface TfaState {
    val retryClicked: LiveData<Boolean>
    val inputEnabled: LiveData<Boolean>
    val nextButtonEnabled: LiveData<Boolean>
    val resendEnabled: LiveData<Boolean>
    var tfaCode: String?
    val tfaError: LiveData<String?>
    val resendText: LiveData<String>
    val tfaTitle: Spanned
    val navigateNextStep: LiveData<Boolean>
    fun onTfaNavigateHandled()
    fun onTfaNextClicked(tfaCredentials: TwoFactorAuthCredentials)
    fun onResendClicked(credentials: TwoFactorAuthCredentials)
}