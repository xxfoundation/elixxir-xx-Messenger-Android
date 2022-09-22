package io.xxlabs.messenger.requests.ui.send

import android.text.Spanned
import io.xxlabs.messenger.ui.dialog.components.CloseButtonUI
import io.xxlabs.messenger.ui.dialog.components.PositiveNegativeButtonUI

interface SendRequestUI : CloseButtonUI, PositiveNegativeButtonUI {
    val body: Spanned
    val senderEmail: String
    val senderPhone: String
    val emailToggleEnabled: Boolean
    val phoneToggleEnabled: Boolean
    fun onEmailToggled(enabled: Boolean)
    fun onPhoneToggled(enabled: Boolean)
}