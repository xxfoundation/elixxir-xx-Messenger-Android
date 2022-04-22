package io.xxlabs.messenger.ui.main.contacts.delete

import io.xxlabs.messenger.ui.dialog.confirm.ConfirmDialogUI

interface DeleteConnectionDialogUI : ConfirmDialogUI {
    val bodyClicked: () -> Unit

    companion object Factory {
        fun create(
            confirmDialog: ConfirmDialogUI,
            bodyClicked: () -> Unit
        ): DeleteConnectionDialogUI {
            return object : DeleteConnectionDialogUI, ConfirmDialogUI by confirmDialog {
                override val bodyClicked = bodyClicked
            }
        }
    }
}