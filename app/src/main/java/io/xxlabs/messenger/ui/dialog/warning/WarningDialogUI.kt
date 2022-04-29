package io.xxlabs.messenger.ui.dialog.warning

import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI

interface WarningDialogUI : InfoDialogUI {
    val buttonText: String
    val buttonOnClick: () -> Unit

    companion object Factory {
        fun create(
            infoDialogUI: InfoDialogUI,
            buttonText: String,
            buttonOnClick: () -> Unit
        ): WarningDialogUI {
            return object : WarningDialogUI, InfoDialogUI by infoDialogUI {
                override val buttonText = buttonText
                override val buttonOnClick = buttonOnClick
            }
        }
    }
}