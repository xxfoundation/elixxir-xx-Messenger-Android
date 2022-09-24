package io.elixxir.feature.splash.ui

import io.elixxir.core.ui.dialog.info.SpanConfig
import io.elixxir.core.ui.dialog.info.TwoButtonInfoDialogUi
import io.elixxir.core.ui.model.UiText

interface VersionAlertUi : TwoButtonInfoDialogUi {
    val dismissable: Boolean
    val downloadUrl: String?
}

data class VersionAlert(
    override val title: UiText,
    override val body: UiText,
    override val positiveLabel: UiText,
    override val negativeLabel: UiText,
    override val onPositiveClick: () -> Unit = {},
    override val onNegativeClick: () -> Unit = {},
    override val onDismissed: () -> Unit = {},
    override val dismissable: Boolean = true,
    override val downloadUrl: String? = null
) : VersionAlertUi {
    override val spans: List<SpanConfig>? = null
}