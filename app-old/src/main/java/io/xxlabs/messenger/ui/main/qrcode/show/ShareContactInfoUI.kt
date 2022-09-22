package io.xxlabs.messenger.ui.main.qrcode.show

import androidx.lifecycle.LiveData

interface ShareContactInfoUI {
    val senderEmail: LiveData<String>
    val senderPhone: LiveData<String>
    val addEmailVisible: Boolean
    val addPhoneVisible: Boolean
    val expandVisible: LiveData<Boolean>
    fun onShowClicked()
    fun onHideClicked()
    fun onCopyClicked()
    fun onEmailToggled(enabled: Boolean)
    fun onPhoneToggled(enabled: Boolean)
    fun onAddEmailClicked()
    fun onAddPhoneClicked()
}