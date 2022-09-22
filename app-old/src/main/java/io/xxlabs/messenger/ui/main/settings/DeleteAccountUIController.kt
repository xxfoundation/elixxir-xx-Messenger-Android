package io.xxlabs.messenger.ui.main.settings

import androidx.lifecycle.LiveData

interface DeleteAccountUIController : DeleteAccountUI {
    val infoClicked: LiveData<Boolean>
    val accountDeleted: LiveData<Boolean>
    val deletionError: LiveData<String?>
    val cancelClicked: LiveData<Boolean>
    fun onAccountDeletedHandled()
    fun onInfoHandled()
    fun onCancelHandled()
}