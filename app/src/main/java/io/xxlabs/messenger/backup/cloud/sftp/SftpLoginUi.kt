package io.xxlabs.messenger.backup.cloud.sftp

import android.text.Editable
import androidx.lifecycle.LiveData

interface SftpLoginUi {
    val hostError: LiveData<String?>
    val maxHostLength: Int
    val portError: LiveData<String?>
    val maxPortLength: Int
    val usernameError: LiveData<String?>
    val maxUsernameLength: Int
    val passwordError: LiveData<String?>
    val maxPasswordLength: Int
    val submitButtonEnabled: LiveData<Boolean>
    val textInputEnabled: LiveData<Boolean>
    fun onHostInput(text: Editable)
    fun onPortInput(text: Editable)
    fun onUsernameInput(text: Editable)
    fun onPasswordInput(text: Editable)
    fun onSubmitClicked()
}