package io.elixxir.core.ui.dialog.warning

import io.elixxir.core.ui.dialog.info.InfoDialogUi
import io.elixxir.core.ui.model.UiText


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