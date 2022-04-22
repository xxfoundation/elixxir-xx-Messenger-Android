package io.xxlabs.messenger.ui.dialog.warning

import androidx.fragment.app.Fragment
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI

/**
 * Launches an ConfirmDialog with a positive button.
 */
@Deprecated("Fragments should receive the DialogUI from their ViewModel.")
fun Fragment.showConfirmDialog(
    title: Int,
    body: Int,
    button: Int,
    action: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val ui = WarningDialogUI.create(
        infoDialogUI = InfoDialogUI.create(
            title = requireContext().getString(title),
            body = requireContext().getString(body),
            null,
            onDismiss
        ),
        buttonText = requireContext().getString(button),
        buttonOnClick = action
    )
    WarningDialog
        .newInstance(ui)
        .show(parentFragmentManager, null)
}

/**
 * Launches an ConfirmDialog with a positive button.
 */
@Deprecated("Fragments should receive the DialogUI from their ViewModel.")
fun Fragment.showConfirmDialog(
    title: String,
    body: String,
    button: String,
    action: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val ui = WarningDialogUI.create(
        infoDialogUI = InfoDialogUI.create(
            title = title,
            body = body,
            null,
            onDismiss
        ),
        buttonText = button,
        buttonOnClick = action
    )
    WarningDialog
        .newInstance(ui)
        .show(parentFragmentManager, null)
}