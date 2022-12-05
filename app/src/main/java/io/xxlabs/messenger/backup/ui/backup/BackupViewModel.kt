package io.xxlabs.messenger.backup.ui.backup

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.data.backup.BackupManager
import io.xxlabs.messenger.backup.data.backup.BackupOption
import io.xxlabs.messenger.backup.data.backup.BackupTaskListener
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialogUI
import io.xxlabs.messenger.ui.dialog.textinput.TextInputDialogUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BackupViewModel(
    val backupManager: BackupManager
) : ViewModel(), BackupPasswordUI, BackupTaskListener {

    abstract fun getBackupOption(): BackupOption?

    private var backupPassword: String = ""

    override val isBackupReady: LiveData<Boolean> by ::_isBackupReady
    private val _isBackupReady = MutableLiveData(true)

    override val isEnabled: LiveData<Boolean> by ::_isEnabled
    private val _isEnabled by lazy { MutableLiveData(getBackupOption()?.isEnabled() ?: false) }

    override val showSetPasswordPrompt: LiveData<TextInputDialogUI?> by ::_showSetPasswordPrompt
    private val _showSetPasswordPrompt = MutableLiveData<TextInputDialogUI?>(null)

    private val passwordInputError = MutableLiveData<String?>(null)
    private val passwordPromptPositiveButtonEnabled = MutableLiveData(false)

    private val passwordPromptUI: TextInputDialogUI by lazy {
        TextInputDialogUI.create(
            BackupPassword.MAX_LENGTH,
            R.string.backup_restore_password_prompt_hint,
            passwordInputError,
            passwordPromptPositiveButtonEnabled,
            ::onBackupPasswordInput,
            TwoButtonInfoDialogUI.create(
                InfoDialogUI.create(
                    appContext().getString(R.string.backup_restore_password_prompt_title),
                    appContext().getString(R.string.backup_restore_password_prompt_body),
                    onDismissed = ::onPasswordPromptDismissed
                ),
                R.string.backup_restore_password_prompt_positive_button,
                R.string.backup_restore_password_prompt_negative_button,
                ::onSubmitPassword,
                {}
            )
        )
    }

    init {
        backupManager.subscribe(getBackupTaskListener())
    }

    override fun backupNow() {
        getBackupOption()?.let {
            backupManager.backupNow(it)
        }
    }

    private fun getBackupTaskListener() = this

    override fun onComplete() {
        _isBackupReady.postValue(true)
    }

    private fun onBackupPasswordInput(password: Editable) {
        with(BackupPassword(password.toString())) {
            if (isValid) {
                backupPassword = value
                passwordPositiveButtonEnabled(true)
            } else {
                passwordPositiveButtonEnabled(false)
            }
            val error = validationError?.let {
                appContext().getString(it)
            }
            showInvalidPasswordError(error)
        }
    }

    private fun passwordPositiveButtonEnabled(enabled: Boolean) {
        passwordPromptPositiveButtonEnabled.value = enabled
    }

    private fun onPasswordPromptDismissed() {
        _isEnabled.value = BackupPassword(backupPassword).isValid
    }

    private fun onSubmitPassword() {
        getBackupOption()?.let {
            _isBackupReady.value = false
            _isEnabled.value = true

            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    backupManager.enableBackup(it, backupPassword)
                }
            }
        }
    }

    private fun showInvalidPasswordError(error: String?) {
        passwordInputError.value = error
    }

    override fun onEnableToggled(enabled: Boolean) {
        when {
            enabled && isEnabled.value == false -> showSetPasswordPrompt()
            enabled && isEnabled.value == true -> return
            else -> disableBackup()
        }
    }

    private fun showSetPasswordPrompt() {
        _showSetPasswordPrompt.value = passwordPromptUI
    }

    private fun disableBackup() {
        getBackupOption()?.let {
            backupManager.disableBackup(it)
        }
        _isEnabled.value = false
    }

    override fun onPasswordPromptHandled() {
        _showSetPasswordPrompt.value = null
    }
}