package io.xxlabs.messenger.ui.main.chat.viewholders

interface WebViewDialogUI {
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