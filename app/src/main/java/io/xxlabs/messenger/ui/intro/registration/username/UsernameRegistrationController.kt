package io.xxlabs.messenger.ui.intro.registration.username

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI

interface UsernameRegistrationController : UsernameRegistrationUI {
    val usernameDialogUI: InfoDialogUI
    val usernameInfoClicked: LiveData<Boolean>
    val usernameNavigateNextStep: LiveData<String?>
    val usernameNavigateDemo: LiveData<Boolean>
    fun onUsernameInfoHandled()
    fun onUsernameNavigateHandled()
}