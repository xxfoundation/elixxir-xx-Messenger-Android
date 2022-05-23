package io.xxlabs.messenger.ui.dialog.info

import io.xxlabs.messenger.R

interface TwoButtonInfoDialogUI : InfoDialogUI {
    val positiveLabel: Int
    val negativeLabel: Int
    val onPositiveClick: () -> Unit
    val onNegativeClick: () -> Unit

    companion object Factory {
        fun create(
            infoDialogUI: InfoDialogUI,
            _positiveLabel: Int = R.string.enable,
            _negativeLabel: Int = R.string.not_now,
            onPositiveClick: () -> Unit,
            onNegativeClick: (() -> Unit)?
        ): TwoButtonInfoDialogUI {
            return object : TwoButtonInfoDialogUI, InfoDialogUI by infoDialogUI {
                override val positiveLabel: Int = _positiveLabel
                override val negativeLabel: Int = _negativeLabel
                override val onPositiveClick = onPositiveClick
                override val onNegativeClick = onNegativeClick ?: {}
            }
        }
    }
}