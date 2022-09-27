package io.elixxir.core.ui.dialog.info

import io.elixxir.core.ui.R
import io.elixxir.core.ui.model.UiText
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
    val url: UiText?

    companion object Factory {
        fun create(
            text: UiText,
            url: UiText?,
            color: Int = R.color.brand_default
        ): SpanConfig {
            return object : SpanConfig {
                override val text: UiText = text
                override val color = color
                override val url: UiText? = url
            }
        }
    }
}