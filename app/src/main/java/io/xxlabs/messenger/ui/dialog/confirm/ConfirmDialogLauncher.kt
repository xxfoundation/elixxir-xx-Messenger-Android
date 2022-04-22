package io.xxlabs.messenger.ui.dialog.confirm

import androidx.fragment.app.Fragment
import io.xxlabs.messenger.ui.dialog.confirm.ConfirmDialog
import io.xxlabs.messenger.ui.dialog.confirm.ConfirmDialogUI
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI

fun Fragment.showConfirmDialog(
    title: Int,
    body: Int,
    button: Int,
    action: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val ui = ConfirmDialogUI.create(
        infoDialogUI = InfoDialogUI.create(
            title = requireContext().getString(title),
            body = requireContext().getString(body),
            null,
            onDismiss
        ),
        buttonText = requireContext().getString(button),
        buttonOnClick = action
    )
    ConfirmDialog
        .newInstance(ui)
        .show(parentFragmentManager, null)
}

fun Fragment.showConfirmDialog(
    title: String,
    body: String,
    button: String,
    action: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val ui = ConfirmDialogUI.create(
        infoDialogUI = InfoDialogUI.create(
            title = title,
            body = body,
            null,
            onDismiss
        ),
        buttonText = button,
        buttonOnClick = action
    )
    ConfirmDialog
        .newInstance(ui)
        .show(parentFragmentManager, null)
}