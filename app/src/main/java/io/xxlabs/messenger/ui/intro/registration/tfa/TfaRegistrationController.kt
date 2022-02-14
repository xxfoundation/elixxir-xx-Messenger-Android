package io.xxlabs.messenger.ui.intro.registration.tfa

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI

interface TfaRegistrationController : TfaRegistrationUI {
    val tfaDialogUI: InfoDialogUI
    val tfaInfoClicked: LiveData<Boolean>
    val tfaRetryClicked: LiveData<Boolean>
    fun onTfaInfoHandled()
    fun onTfaNavigateNextStep(tfaCredentials: TwoFactorAuthCredentials): LiveData<Boolean>
    fun onTfaNavigateHandled(tfaCredentials: TwoFactorAuthCredentials)
}