package io.xxlabs.messenger.dialog.info

import io.xxlabs.messenger.util.UiText

interface TwoButtonInfoDialogUi : InfoDialogUi {
    val positiveLabel: UiText
    val negativeLabel: UiText
    val onPositiveClick: () -> Unit
    val onNegativeClick: () -> Unit

    companion object Factory {
        fun create(
            infoDialogUi: InfoDialogUi,
            _positiveLabel: UiText = UiText.StringResource(android.R.string.ok),
            _negativeLabel: UiText = UiText.StringResource(android.R.string.cancel),
            onPositiveClick: () -> Unit,
            onNegativeClick: (() -> Unit)?
        ): TwoButtonInfoDialogUi {
            return object : TwoButtonInfoDialogUi, InfoDialogUi by infoDialogUi {
                override val positiveLabel: UiText = _positiveLabel
                override val negativeLabel: UiText = _negativeLabel
                override val onPositiveClick = onPositiveClick
                override val onNegativeClick = onNegativeClick ?: {}
            }
        }
    }
}