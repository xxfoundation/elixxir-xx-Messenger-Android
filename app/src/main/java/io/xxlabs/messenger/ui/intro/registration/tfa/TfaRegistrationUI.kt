package io.xxlabs.messenger.ui.intro.registration.tfa

import android.text.Spanned
import androidx.lifecycle.LiveData

interface TfaRegistrationUI {
    val maxTfaInputLength: Int
    var tfaInput: String?
    val tfaError: LiveData<String?>
    val resendText: LiveData<String>
    fun getTfaTitle(tfaCredentials: TwoFactorAuthCredentials): Spanned
    fun getTfaBody(tfaCredentials: TwoFactorAuthCredentials): String
    fun isTfaInputEnabled(tfaCredentials: TwoFactorAuthCredentials): LiveData<Boolean>
    fun isTfaNextButtonEnabled(tfaCredentials: TwoFactorAuthCredentials): LiveData<Boolean>
    fun onTfaNextClicked(tfaCredentials: TwoFactorAuthCredentials)
    fun isResendEnabled(tfaCredentials: TwoFactorAuthCredentials): LiveData<Boolean>
    fun onResendClicked(tfaCredentials: TwoFactorAuthCredentials)
    fun onTfaInfoClicked()
}