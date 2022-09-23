package io.elixxir.core.ui.dialog.info

import io.xxlabs.messenger.R
import io.xxlabs.messenger.util.UiText
import java.io.Serializable

interface InfoDialogUi : Serializable {
    val title: UiText
    val body: UiText
    val spans: List<SpanConfig>?
    val onDismissed: (() -> Unit)?

    companion object Factory {
        fun create(
            title: UiText,
            body: UiText,
            spans: List<SpanConfig>? = null,
            onDismissed: () -> Unit = { }
        ): InfoDialogUi {
            return object : InfoDialogUi {
                override val title = title
                override val body: UiText = body
                override val spans = spans
                override val onDismissed = onDismissed
            }
        }
    }
}

interface SpanConfig : Serializable {
    val text: UiText
    val color: Int
    val url: String?

    companion object Factory {
        fun create(
            text: UiText,
            url: String?,
            color: Int = R.color.brand_default
        ): SpanConfig {
            return object : SpanConfig {
                override val text: UiText = text
                override val color = color
                override val url: String? = url
            }
        }
    }
}