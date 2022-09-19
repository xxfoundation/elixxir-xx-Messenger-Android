package io.xxlabs.messenger.dialog.warning

import io.xxlabs.messenger.dialog.info.InfoDialogUi
import io.xxlabs.messenger.util.UiText

interface WarningDialogUi : InfoDialogUi {
    val buttonText: UiText
    val buttonOnClick: () -> Unit

    companion object Factory {
        fun create(
            infoDialogUi: InfoDialogUi,
            buttonText: UiText,
            buttonOnClick: () -> Unit
        ): WarningDialogUi {
            return object : WarningDialogUi, InfoDialogUi by infoDialogUi {
                override val buttonText: UiText = buttonText
                override val buttonOnClick = buttonOnClick
            }
        }
    }
}