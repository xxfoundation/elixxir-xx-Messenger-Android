package io.xxlabs.messenger.ui.main.contacts.delete

import io.xxlabs.messenger.ui.dialog.warning.WarningDialogUI

interface DeleteConnectionDialogUI : WarningDialogUI {
    val bodyClicked: () -> Unit

    companion object Factory {
        fun create(
            warningDialog: WarningDialogUI,
            bodyClicked: () -> Unit
        ): DeleteConnectionDialogUI {
            return object : DeleteConnectionDialogUI, WarningDialogUI by warningDialog {
                override val bodyClicked = bodyClicked
            }
        }
    }
}