package io.xxlabs.messenger.backup.cloud.crust.login.ui

import android.text.Editable
import androidx.lifecycle.LiveData

interface CrustLoginUi {
    val submitButtonEnabled: LiveData<Boolean>
    val textInputEnabled: LiveData<Boolean>
    val maxUsernameLength: Int get() = 256
    fun onUsernameInput(text: Editable)
    fun onSubmitClicked()
}