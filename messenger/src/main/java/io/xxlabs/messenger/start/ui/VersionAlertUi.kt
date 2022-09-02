package io.xxlabs.messenger.start.ui

interface VersionAlertUi {
    val title: String
    val subtitle: String
    val positiveLabel: String
    val negativeLabel: String
    val onPositiveClick: () -> Unit
    val onNegativeClick: () -> Unit
    val onDismiss: () -> Unit
    val dismissable: Boolean
}


data class VersionAlert(
    override val title: String,
    override val subtitle: String,
    override val positiveLabel: String,
    override val negativeLabel: String,
    override val onPositiveClick: () -> Unit = {},
    override val onNegativeClick: () -> Unit = {},
    override val onDismiss: () -> Unit = {},
    override val dismissable: Boolean = true
) : VersionAlertUi