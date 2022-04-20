package io.xxlabs.messenger.backup.ui.restore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.xxlabs.messenger.backup.model.RestoreOption
import io.xxlabs.messenger.backup.ui.save.EditTextTwoButtonDialogUI
import java.io.Serializable

interface RestorePasswordUI {
    val showEnterPasswordPrompt: LiveData<EditTextTwoButtonDialogUI?>
    fun onPasswordPromptHandled()
}

interface RestoreDetailUI : RestorePasswordUI {
    val backup: RestoreOption
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
    val restore: RestoreOption
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