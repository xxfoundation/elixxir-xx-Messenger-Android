package io.xxlabs.messenger.support.dialog.info

import io.xxlabs.messenger.R

interface InfoDialogUI {
    val title: String
    val body: String
    val spans: List<SpanConfig>?
    val onDismissed: (() -> Unit)?

    companion object Factory {
        fun create(
            title: String,
            body: String,
            spans: List<SpanConfig>? = null,
            onDismissed: () -> Unit = { }
        ): InfoDialogUI {
            return object : InfoDialogUI {
                override val title = title
                override val body = body
                override val spans = spans
                override val onDismissed = onDismissed
            }
        }
    }
}

interface SpanConfig {
    val text: String
    val color: Int
    val url: String?

    companion object Factory {
        fun create(
            text: String,
            url: String?,
            color: Int = R.color.brand_default
        ): SpanConfig {
            return object : SpanConfig {
                override val text = text
                override val color = color
                override val url: String? = url
            }
        }
    }
}