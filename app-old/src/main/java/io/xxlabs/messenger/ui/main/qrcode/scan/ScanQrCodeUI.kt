package io.xxlabs.messenger.ui.main.qrcode.scan

import android.text.Spannable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import io.xxlabs.messenger.R
import io.xxlabs.messenger.ui.main.chat.setVisibility

interface ScanQrCodeUI {
    val hasCameraPermission: Boolean
    val scanState: ScanState
    val description: Spannable
    val callToActionText: String?
    fun onCtaClicked()
    fun onPermissionWarningClicked()
}

/**
 * Defines possible states of the QR scanner and its properties
 */
sealed class ScanState {
    abstract val accentColor: Int
    abstract val icon: Int?

    data class Ready(
        override val accentColor: Int = R.color.brand_default,
        override val icon: Int? = null
    ): ScanState()

    data class Scanning(
        override val accentColor: Int = R.color.brand_default,
        override val icon: Int? = null
    ): ScanState()

    data class Successful(
        override val accentColor: Int = R.color.accent_success,
        override val icon: Int? = R.drawable.ic_check
    ): ScanState()

    data class Error(
        override val accentColor: Int = R.color.accent_danger,
        override val icon: Int? = R.drawable.ic_error_red_24dp
    ): ScanState()
}

@BindingAdapter("scanStateIcon")
fun ImageView.setScanStateIcon(state: ScanState) {
    state.icon?.let {
        visibility = View.VISIBLE
        setImageResource(it)
    } ?: run { visibility = View.INVISIBLE }
}

@BindingAdapter("qrCodeAnimationVisibility")
fun ImageView.setVisibility(state: ScanState) {
    setVisibility(state is ScanState.Scanning)
}