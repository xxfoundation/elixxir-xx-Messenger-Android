package io.xxlabs.messenger.ui.dialog.confirm

import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI

interface ConfirmDialogUI : InfoDialogUI {
    val buttonText: String
    val buttonOnClick: () -> Unit

    companion object Factory {
        fun create(
            infoDialogUI: InfoDialogUI,
            buttonText: String,
            buttonOnClick: () -> Unit
        ): ConfirmDialogUI {
            return object : ConfirmDialogUI, InfoDialogUI by infoDialogUI {
                override val buttonText = buttonText
                override val buttonOnClick = buttonOnClick
            }
        }
    }
}