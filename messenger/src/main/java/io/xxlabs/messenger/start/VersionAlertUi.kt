package io.xxlabs.messenger.start

import io.xxlabs.messenger.R

interface VersionAlertUi {
    val title: Int
    val subtitle: Int
    val positiveLabel: Int
    val negativeLabel: Int
    val onPositiveClick: () -> Unit
    val onNegativeClick: () -> Unit
    val onDismiss: () -> Unit
    val dismissable: Boolean
}


data class VersionAlert(
    override val title: Int = R.string.version_alert_update_recommended_title,
    override val subtitle: Int = R.string.version_alert_update_recommended_subtitle,
    override val positiveLabel: Int = R.string.version_alert_update_recommended_positive_label,
    override val negativeLabel: Int = R.string.version_alert_update_recommended_negative_label,
    override val onPositiveClick: () -> Unit = {},
    override val onNegativeClick: () -> Unit = {},
    override val onDismiss: () -> Unit = {},
    override val dismissable: Boolean = true
) : VersionAlertUi