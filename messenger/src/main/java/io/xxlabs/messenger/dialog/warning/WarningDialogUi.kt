package io.xxlabs.messenger.dialog.warning

import io.xxlabs.messenger.dialog.info.InfoDialogUi

interface WarningDialogUi : InfoDialogUi {
    val buttonText: String
    val buttonOnClick: () -> Unit

    companion object Factory {
        fun create(
            infoDialogUI: InfoDialogUi,
            buttonText: String,
            buttonOnClick: () -> Unit
        ): WarningDialogUi {
            return object : WarningDialogUi, InfoDialogUi by infoDialogUI {
                override val buttonText = buttonText
                override val buttonOnClick = buttonOnClick
            }
        }
    }
}