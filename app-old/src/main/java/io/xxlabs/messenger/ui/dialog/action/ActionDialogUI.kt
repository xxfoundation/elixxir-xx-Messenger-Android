package io.xxlabs.messenger.ui.dialog.action

import io.xxlabs.messenger.ui.dialog.warning.WarningDialogUI

interface ActionDialogUI : WarningDialogUI {
    
    companion object Factory {
        fun create(warningDialogUI: WarningDialogUI) : ActionDialogUI {
            return object : ActionDialogUI, WarningDialogUI by warningDialogUI {}
        }
    }
}