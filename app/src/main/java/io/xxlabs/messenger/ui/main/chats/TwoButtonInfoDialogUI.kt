package io.xxlabs.messenger.ui.main.chats

import io.xxlabs.messenger.support.dialog.info.InfoDialogUI

interface TwoButtonInfoDialogUI : InfoDialogUI {
    val onPositiveClick: () -> Unit
    val onNegativeClick: (() -> Unit)

    companion object Factory {
        fun create(
            infoDialogUI: InfoDialogUI,
            onPositiveClick: () -> Unit,
            onNegativeClick: (() -> Unit)?
        ): TwoButtonInfoDialogUI {
            return object : TwoButtonInfoDialogUI, InfoDialogUI by infoDialogUI {
                override val onPositiveClick = onPositiveClick
                override val onNegativeClick = onNegativeClick ?: {}
            }
        }
    }
}