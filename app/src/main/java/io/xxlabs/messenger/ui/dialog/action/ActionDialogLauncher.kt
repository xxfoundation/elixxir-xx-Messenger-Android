package io.xxlabs.messenger.ui.dialog.action

import androidx.fragment.app.Fragment
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.dialog.warning.WarningDialogUI

/**
 * Launches an ActionDialog with a positive button.
 */
@Deprecated("Fragments should receive the DialogUI from their ViewModel.")
fun Fragment.showActionDialog(
    title: Int,
    body: Int,
    button: Int,
    action: () -> Unit
) {
    val ui = ActionDialogUI.create(
        WarningDialogUI.create(
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
