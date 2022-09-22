package io.xxlabs.messenger.ui.main.qrcode.scan

import android.text.Spannable
import android.text.SpannableString
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.appContext

val defaultDescription: Spannable =
    SpannableString(appContext().getString(R.string.scan_qr_ready_description))

data class ScanQrCode(
    override val hasCameraPermission: Boolean = false,
    override val scanState: ScanState = ScanState.Ready(),
    override val description: Spannable = defaultDescription,
    override val callToActionText: String? = null,
    private val _onCtaClicked: () -> Unit = {},
    private val _onPermissionWarningClicked: () -> Unit = {}
): ScanQrCodeUI {
    override fun onCtaClicked() = _onCtaClicked()
    override fun onPermissionWarningClicked() = _onPermissionWarningClicked()
}