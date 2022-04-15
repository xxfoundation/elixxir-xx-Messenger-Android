package io.xxlabs.messenger.ui.intro.registration.email

import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.intro.registration.tfa.TwoFactorAuthCredentials

interface EmailRegistrationUI {
    val emailTitle: Spanned
    val email: MutableLiveData<String?>
    val maxEmailLength: Int
    val emailError: LiveData<String?>
    val emailNextButtonEnabled: LiveData<Boolean>
    val emailSkipButtonEnabled: LiveData<Boolean>
    val emailInputEnabled: LiveData<Boolean>
    fun onEmailInfoClicked()
    fun onEmailNextClicked()
    fun onEmailSkipClicked()
}

interface EmailRegistrationController : EmailRegistrationUI {
    val emailDialogUI: InfoDialogUI
    val emailInfoClicked: LiveData<Boolean>
    val emailNavigateNextStep: LiveData<TwoFactorAuthCredentials?>
    val emailNavigateSkip: LiveData<Boolean>
    fun onEmailInfoHandled()
    fun onEmailNavigateHandled()
}