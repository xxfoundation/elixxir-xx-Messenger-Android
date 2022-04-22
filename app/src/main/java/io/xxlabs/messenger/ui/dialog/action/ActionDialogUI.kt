package io.xxlabs.messenger.ui.dialog.action

import io.xxlabs.messenger.support.dialog.confirm.ConfirmDialogUI

interface ActionDialogUI : ConfirmDialogUI {
    
    companion object Factory {
        fun create(confirmDialogUI: ConfirmDialogUI) : ActionDialogUI {
            return object : ActionDialogUI, ConfirmDialogUI by confirmDialogUI {}
        }
    }
}