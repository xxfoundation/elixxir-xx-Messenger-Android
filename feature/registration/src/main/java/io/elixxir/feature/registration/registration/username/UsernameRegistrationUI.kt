package io.elixxir.feature.registration.registration.username

import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import androidx.lifecycle.LiveData
import io.elixxir.core.ui.dialog.info.InfoDialogUi
import io.elixxir.core.ui.model.UiText

interface UsernameRegistrationUI {
    val usernameTitle: Spanned
    val maxUsernameLength: Int
    val usernameError: LiveData<UiText?>
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
    val usernameDialogUI: InfoDialogUi
    val usernameInfoClicked: LiveData<Boolean>
    val usernameNavigateNextStep: LiveData<String?>
    val usernameNavigateDemo: LiveData<Boolean>
    val usernameNavigateRestore: LiveData<Boolean>
    fun onUsernameInfoHandled()
    fun onUsernameNavigateHandled()
}