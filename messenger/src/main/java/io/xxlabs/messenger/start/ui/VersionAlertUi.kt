package io.xxlabs.messenger.start.ui

import io.xxlabs.messenger.util.UiText

interface VersionAlertUi {
    val title: UiText
    val subtitle: UiText
    val positiveLabel: UiText
    val negativeLabel: UiText
    val onPositiveClick: () -> Unit
    val onNegativeClick: () -> Unit
    val onDismiss: () -> Unit
    val dismissable: Boolean
    val downloadUrl: String?
}


data class VersionAlert(
    override val title: UiText,
    override val subtitle: UiText,
    override val positiveLabel: UiText,
    override val negativeLabel: UiText,
    override val onPositiveClick: () -> Unit = {},
    override val onNegativeClick: () -> Unit = {},
    override val onDismiss: () -> Unit = {},
    override val dismissable: Boolean = true,
    override val downloadUrl: String? = null
) : VersionAlertUi