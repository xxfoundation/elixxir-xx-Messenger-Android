package io.xxlabs.messenger.ui

import androidx.fragment.app.FragmentManager
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.dialog.confirm.ConfirmDialog
import io.xxlabs.messenger.support.dialog.confirm.ConfirmDialogUI
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI

class ConfirmDialogLauncher(private val fragmentMgr: FragmentManager) {

    fun showConfirmDialog(
        title: Int,
        body: Int,
        button: Int,
        action: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        val ui = ConfirmDialogUI.create(
            infoDialogUI = InfoDialogUI.create(
                title = appContext().getString(title),
                body = appContext().getString(body),
                null,
                onDismiss
            ),
            buttonText = appContext().getString(button),
            buttonOnClick = action
        )
        ConfirmDialog.newInstance(ui)
            .show(fragmentMgr, null)
    }

    fun showConfirmDialog(
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
        ConfirmDialog.newInstance(ui)
            .show(fragmentMgr, null)
    }
}