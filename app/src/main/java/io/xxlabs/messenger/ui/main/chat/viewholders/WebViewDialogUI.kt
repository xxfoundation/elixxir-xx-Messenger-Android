package io.xxlabs.messenger.ui.main.chat.viewholders

import java.io.Serializable

interface WebViewDialogUI : Serializable {
    val url: String
    val onDismissed: (() -> Unit)?

    companion object Factory {
        fun create(url: String, onDismissed: (() -> Unit)?): WebViewDialogUI {
            return object : WebViewDialogUI {
                override val url: String = url
                override val onDismissed: (() -> Unit)? = onDismissed
            }
        }
    }
}