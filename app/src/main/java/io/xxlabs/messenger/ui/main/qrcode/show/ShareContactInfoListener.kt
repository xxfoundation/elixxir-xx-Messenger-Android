package io.xxlabs.messenger.ui.main.qrcode.show

import androidx.lifecycle.LiveData

interface ShareContactInfoListener {
    val expandVisible: LiveData<Boolean>
    fun onExpandClicked()
    fun onCollapseClicked()
    fun onCopyClicked()
    fun onEmailToggled(enabled: Boolean)
    fun onPhoneToggled(enabled: Boolean)
    fun onAddEmailClicked()
    fun onAddPhoneClicked()
}