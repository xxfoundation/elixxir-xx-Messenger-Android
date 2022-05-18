package io.xxlabs.messenger.support.toast

import com.google.android.material.snackbar.Snackbar
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.appContext

interface ToastUI {
    val backgroundColor: Int
    val header: String?
    val body: String?
    val actionText: String?
    val leftIcon: Int?
    val iconTint: Int?
    val duration: Int

    fun onActionClick()

    companion object Factory {
        fun create(
            body: String,
            header: String? = null,
            actionText: String? = null,
            backgroundColor: Int = appContext().getColor(R.color.modal_overlay),
            leftIcon: Int = R.drawable.ic_mail_sent,
            iconTint: Int = R.color.neutral_white,
            duration: Int = Snackbar.LENGTH_LONG,
            actionClick: () -> Unit = {}
        ) : ToastUI = object : ToastUI {
            override val backgroundColor: Int = backgroundColor
            override val header: String? = header
            override val body: String? = body
            override val actionText: String? = actionText
            override val leftIcon: Int? = leftIcon
            override val iconTint: Int? = iconTint
            override val duration: Int = duration

            override fun onActionClick() = actionClick()
        }
    }
}