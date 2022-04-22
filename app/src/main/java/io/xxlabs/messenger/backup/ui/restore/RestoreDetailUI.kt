package io.xxlabs.messenger.backup.ui.restore

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.data.restore.RestoreLog
import io.xxlabs.messenger.backup.model.AccountBackup
import io.xxlabs.messenger.backup.ui.dialog.TextInputDialogUI
import java.io.Serializable

interface RestorePasswordUI {
    val showEnterPasswordPrompt: LiveData<TextInputDialogUI?>
    fun onPasswordPromptHandled()
}

interface RestoreDetailUI : RestorePasswordUI {
    val backup: AccountBackup
    val state: LiveData<RestoreState>
    val isLoading: LiveData<Boolean>
}

interface RestoreDetailController : RestoreDetailUI {
    val restoreComplete: LiveData<Boolean>
    val cancelRestore: LiveData<Boolean>
    val restoreError: LiveData<String?>
    fun onErrorHandled()
}

sealed interface RestoreState : Serializable

interface RestoreReady : RestoreState {
    val onNextClicked: () -> Unit
    val onCancelClicked: () -> Unit

    companion object Factory {
        fun create(onNext: () -> Unit, onCancel: () -> Unit) =
            object : RestoreReady {
                override val onNextClicked = onNext
                override val onCancelClicked = onCancel
            }
    }
}

interface RestoreStarted: RestoreState {
    val accountBackup: AccountBackup
    val restoreLog: RestoreLog?
}

interface RestoreSuccess : RestoreState {
    val onNextClicked: () -> Unit

    companion object Factory {
        fun create(onNext: () -> Unit) =
            object : RestoreSuccess {
                override val onNextClicked = onNext
            }
    }
}