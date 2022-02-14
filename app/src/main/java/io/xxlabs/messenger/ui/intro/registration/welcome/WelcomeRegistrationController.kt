package io.xxlabs.messenger.ui.intro.registration.welcome

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI

interface WelcomeRegistrationController : WelcomeRegistrationUI {
    val welcomeDialogUI: InfoDialogUI
    val welcomeInfoClicked: LiveData<Boolean>
    val welcomeNavigateNext: LiveData<Boolean>
    val welcomeNavigateSkip: LiveData<Boolean>
    fun onWelcomeInfoHandled()
    fun onWelcomeNavigateHandled()
}