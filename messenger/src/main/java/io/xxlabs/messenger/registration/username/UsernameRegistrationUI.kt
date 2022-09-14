package io.xxlabs.messenger.ui.intro.registration.username

import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI

interface UsernameRegistrationUI {
    val usernameTitle: Spanned
    val maxUsernameLength: Int
    val usernameError: LiveData<String?>
    val usernameNextButtonEnabled: LiveData<Boolean>
    val usernameInputEnabled: LiveData<Boolean>
    val usernameFilters: Array<InputFilter>
    val restoreEnabled: LiveData<Boolean>
    fun onUsernameInfoClicked()
    fun onUsernameNextClicked()
    fun onRestoreAccountClicked()
    fun onUsernameInput(text: Editable)
}

interface UsernameRegistrationController : UsernameRegistrationUI {
    val usernameDialogUI: InfoDialogUI
    val usernameInfoClicked: LiveData<Boolean>
    val usernameNavigateNextStep: LiveData<String?>
    val usernameNavigateDemo: LiveData<Boolean>
    val usernameNavigateRestore: LiveData<Boolean>
    fun onUsernameInfoHandled()
    fun onUsernameNavigateHandled()
}