package io.xxlabs.messenger.start.ui

import io.xxlabs.messenger.dialog.info.SpanConfig
import io.xxlabs.messenger.dialog.info.TwoButtonInfoDialogUi
import io.xxlabs.messenger.util.UiText

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