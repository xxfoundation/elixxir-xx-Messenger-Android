package io.xxlabs.messenger.ui.dialog

import androidx.fragment.app.Fragment
import io.xxlabs.messenger.support.dialog.action.ActionDialog
import io.xxlabs.messenger.support.dialog.action.ActionDialogUI
import io.xxlabs.messenger.support.dialog.confirm.ConfirmDialogUI
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI

fun Fragment.showActionDialog(
    title: Int,
    body: Int,
    button: Int,
    action: () -> Unit
) {
    val ui = ActionDialogUI.create(
        ConfirmDialogUI.create(
            infoDialogUI = InfoDialogUI.create(
                title = getString(title),
                body = getString(body),
            ),
            buttonText = getString(button),
            buttonOnClick = action
        )
    )
    ActionDialog.newInstance(ui)
        .show(parentFragmentManager, null)
}
