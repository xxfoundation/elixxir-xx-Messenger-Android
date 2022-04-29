package io.xxlabs.messenger.ui.intro.registration.welcome

import android.text.Spanned
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI

interface WelcomeRegistrationUI {
    fun welcomeTitle(username: String): Spanned
    fun onWelcomeInfoClicked()
    fun onWelcomeNextClicked()
    fun onWelcomeSkipClicked()
}

interface WelcomeRegistrationController : WelcomeRegistrationUI {
    val welcomeDialogUI: InfoDialogUI
    val welcomeInfoClicked: LiveData<Boolean>
    val welcomeNavigateNext: LiveData<Boolean>
    val welcomeNavigateSkip: LiveData<Boolean>
    fun onWelcomeInfoHandled()
    fun onWelcomeNavigateHandled()
}