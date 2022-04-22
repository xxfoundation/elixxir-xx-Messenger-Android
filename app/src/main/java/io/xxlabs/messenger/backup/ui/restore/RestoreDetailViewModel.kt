package io.xxlabs.messenger.backup.ui.restore

import android.text.Editable
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.data.restore.RestoreEnvironment
import io.xxlabs.messenger.backup.data.restore.RestoreLog
import io.xxlabs.messenger.backup.data.restore.RestoreManager
import io.xxlabs.messenger.backup.model.AccountBackup
import io.xxlabs.messenger.backup.ui.backup.BackupPassword
import io.xxlabs.messenger.backup.ui.dialog.TextInputDialogUI
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.main.chats.TwoButtonInfoDialogUI
import kotlinx.coroutines.*

class RestoreDetailViewModel @AssistedInject constructor(
    private val restoreManager: RestoreManager,
    @Assisted override val backup: AccountBackup,
    @Assisted private val restorePassword: ByteArray,
): ViewModel(), RestoreDetailController {

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        exception.message?.let {
            val errorMessage =
                if (it.contains("chacha20poly1305")) "Incorrect password."
                else it
            _restoreError.postValue(errorMessage)
        }
        restoreManager.cancelRestore(backup)
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
        override val accountBackup: AccountBackup = backup
        override val restoreLog: RestoreLog?
            get() = restoreManager.getRestoreLog(backup)
    }

    private val success: RestoreSuccess get() =
        RestoreSuccess.create { _restoreComplete.value = true }

    private var restoreTask: Job? = null

    private fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    override val showEnterPasswordPrompt: LiveData<TextInputDialogUI?> by ::_showEnterPasswordPrompt
    private val _showEnterPasswordPrompt = MutableLiveData<TextInputDialogUI?>(null)

    private val passwordInputError = MutableLiveData<String?>(null)
    private val passwordPromptPositiveButtonEnabled = MutableLiveData(true)

    private val passwordPromptUI: TextInputDialogUI by lazy {
        TextInputDialogUI.create(
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
            restoreManager.restore(backup, restoreEnvironment)
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
            backup: AccountBackup,
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
    fun create(backup: AccountBackup, restorePassword: ByteArray): RestoreDetailViewModel
}