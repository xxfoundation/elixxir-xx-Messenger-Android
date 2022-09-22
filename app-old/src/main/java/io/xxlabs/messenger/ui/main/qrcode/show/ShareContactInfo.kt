package io.xxlabs.messenger.ui.main.qrcode.show

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.requests.ui.send.RequestSender
import io.xxlabs.messenger.support.appContext

class ShareContactInfo(
    private val sender: RequestSender,
    private val listener: ShareContactInfoListener
) : ShareContactInfoUI {
    override val senderEmail: LiveData<String> by ::_senderEmail
    private val _senderEmail = MutableLiveData(obfuscate(sender.email) ?: getPlaceholder())
    override val senderPhone: LiveData<String> by ::_senderPhone
    private val _senderPhone = MutableLiveData(obfuscate(formattedPhone()) ?: getPlaceholder())
    override val addEmailVisible: Boolean = sender.email.isNullOrBlank()
    override val addPhoneVisible: Boolean = sender.phone.isNullOrBlank()
    override val expandVisible: LiveData<Boolean> = listener.expandVisible

    private fun obfuscate(text: String?): String? {
        if (text.isNullOrBlank()) return null
        return "•".repeat(10)
    }

    override fun onShowClicked() = listener.onExpandClicked()

    override fun onHideClicked() = listener.onCollapseClicked()

    override fun onCopyClicked() = listener.onCopyClicked()

    private fun formattedPhone(): String? = sender.phone?.let { Country.toFormattedNumber(it) }

    private fun getPlaceholder(): String =
        appContext().getString(R.string.share_contact_info_placeholder)

    override fun onEmailToggled(enabled: Boolean) {
        _senderEmail.value = if (enabled) sender.email else obfuscate(sender.email)
        listener.onEmailToggled(enabled)
    }

    override fun onPhoneToggled(enabled: Boolean) {
        _senderPhone.value = if (enabled) formattedPhone() else obfuscate(formattedPhone())
        listener.onPhoneToggled(enabled)
    }

    override fun onAddEmailClicked() = listener.onAddEmailClicked()

    override fun onAddPhoneClicked() = listener.onAddPhoneClicked()
}