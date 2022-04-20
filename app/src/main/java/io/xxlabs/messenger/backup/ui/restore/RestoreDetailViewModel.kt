package io.xxlabs.messenger.backup.ui.restore

import android.text.Editable
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.data.BackupDataSource
import io.xxlabs.messenger.backup.model.RestoreEnvironment
import io.xxlabs.messenger.backup.model.RestoreOption
import io.xxlabs.messenger.backup.ui.save.BackupPassword
import io.xxlabs.messenger.backup.ui.save.EditTextTwoButtonDialogUI
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.main.chats.TwoButtonInfoDialogUI
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException

class RestoreDetailViewModel @AssistedInject constructor(
    private val dataSource: BackupDataSource<RestoreOption>,
    @Assisted override val backup: RestoreOption,
    @Assisted private val restorePassword: ByteArray,
): ViewModel(), RestoreDetailController {

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        exception.message?.let {
            val errorMessage =
                if (it.contains("chacha20poly1305")) "Incorrect password."
                else it
            _restoreError.postValue(errorMessage)
        }
        backup.cancelRestore()
    }

    override val restoreComplete: LiveData<Boolean> by ::_restoreComplete
    private val _restoreComplete = MutableLiveData(false)

    override val cancelRestore: LiveData<Boolean> by ::_cancelRestore
    private val _cancelRestore = MutableLiveData(false)

    override val restoreError: LiveData<String?> by ::_restoreError
    private val _restoreError = MutableLiveData<String?>()

    override val isLoading: LiveData<Boolean> by ::_isLoading
    private val _isLoading = MutableLiveData(false)

    override val state: LiveData<RestoreState> get() =
        Transformations.map(backup.progress) { progress ->
            progress?.run {
                when (bytesTransferred) {
                    bytesTotal -> success
                    else -> startedState.also { setLoading(false) }
                }
            } ?: readyState
        }

    private val readyState: RestoreReady get() =
        RestoreReady.create(
            ::showEnterPasswordPrompt,
            ::cancelRestore
        )

    private val startedState: RestoreStarted = object : RestoreStarted {
        override val restore: RestoreOption = backup
    }

    private val success: RestoreSuccess get() =
        RestoreSuccess.create { _restoreComplete.value = true }

    private var restoreTask: Job? = null

    private fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    override val showEnterPasswordPrompt: LiveData<EditTextTwoButtonDialogUI?> by ::_showEnterPasswordPrompt
    private val _showEnterPasswordPrompt = MutableLiveData<EditTextTwoButtonDialogUI?>(null)

    private val passwordInputError = MutableLiveData<String?>(null)
    private val passwordPromptPositiveButtonEnabled = MutableLiveData(true)

    private val passwordPromptUI: EditTextTwoButtonDialogUI by lazy {
        EditTextTwoButtonDialogUI.create(
            BackupPassword.MAX_LENGTH,
            R.string.backup_restore_password_restore_hint,
            passwordInputError,
            passwordPromptPositiveButtonEnabled,
            ::onBackupPasswordInput,
            TwoButtonInfoDialogUI.create(
                InfoDialogUI.create(
                    appContext().getString(R.string.backup_restore_password_restore_title),
                    appContext().getString(R.string.backup_restore_password_restore_body),
                    onDismissed = ::onPasswordPromptDismissed
                ),
                R.string.backup_restore_password_prompt_positive_button,
                R.string.backup_restore_password_prompt_negative_button,
                ::attemptRestore,
                {}
            )
        )
    }

    private var backupPassword = ""

    private fun onBackupPasswordInput(password: Editable) {
        backupPassword = password.toString()
    }

    private fun onPasswordPromptDismissed() {}

    private fun showEnterPasswordPrompt() {
        _showEnterPasswordPrompt.value = passwordPromptUI
    }

    override fun onPasswordPromptHandled() {
        _showEnterPasswordPrompt.value = null
    }

    private fun attemptRestore() {
        setLoading(true)
        restoreTask?.let { return@let }
        restoreTask = viewModelScope.launch(exceptionHandler) {
            backup.restore(restoreEnvironment)
        }
    }

    private fun cancelRestore() {
        _cancelRestore.value = true
    }

    override fun onErrorHandled() {
        _restoreError.value = null
    }

    private val restoreEnvironment: RestoreEnvironment
        get() {
            return RestoreEnvironment(
                BindingsWrapperBindings.getNdf(),
                BindingsWrapperBindings.createSessionFolder(appContext()).path,
                restorePassword,
                backupPassword.encodeToByteArray(),
            )
        }

    companion object {
        fun provideFactory(
            assistedFactory: BackupFoundViewModelFactory,
            backup: RestoreOption,
            restorePassword: ByteArray
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(backup, restorePassword) as T
            }
        }
    }
}

@AssistedFactory
interface BackupFoundViewModelFactory {
    fun create(backup: RestoreOption, restorePassword: ByteArray): RestoreDetailViewModel
}