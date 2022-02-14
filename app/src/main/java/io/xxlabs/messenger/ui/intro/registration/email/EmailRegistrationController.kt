package io.xxlabs.messenger.ui.intro.registration.email

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.intro.registration.tfa.TwoFactorAuthCredentials

interface EmailRegistrationController : EmailRegistrationUI {
    val emailDialogUI: InfoDialogUI
    val emailInfoClicked: LiveData<Boolean>
    val emailNavigateNextStep: LiveData<TwoFactorAuthCredentials?>
    val emailNavigateSkip: LiveData<Boolean>
    fun onEmailInfoHandled()
    fun onEmailNavigateHandled()
}