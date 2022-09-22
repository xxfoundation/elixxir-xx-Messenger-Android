package io.xxlabs.messenger.ui.main.settings

import androidx.lifecycle.LiveData

interface DeleteAccountUI {
    var confirmDeleteInput: String
    val inputError: LiveData<String?>
    val confirmEnabled: LiveData<Boolean>
    val loading: LiveData<Boolean>
    fun onInfoClicked()
    fun onConfirmDeleteClicked()
    fun onCancelClicked()
}