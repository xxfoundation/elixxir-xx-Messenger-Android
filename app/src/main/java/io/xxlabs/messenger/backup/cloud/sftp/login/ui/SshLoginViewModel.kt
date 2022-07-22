package io.xxlabs.messenger.backup.cloud.sftp.login.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.xxlabs.messenger.backup.cloud.sftp.login.SshCredentials
import timber.log.Timber

class SshLoginViewModel : ViewModel(), SshLoginListener {

    val sshLoginUi: LiveData<SshLoginUi> by ::_sshLoginUi
    private val _sshLoginUi = MutableLiveData<SshLoginUi>(SshLogin(this))

    val loginSuccess: LiveData<SshCredentials?> by ::_loginSuccess
    private val _loginSuccess = MutableLiveData<SshCredentials?>(null)

    val loginError: LiveData<String?> by ::_loginError
    private val _loginError = MutableLiveData<String?>(null)

    override fun onLoginSuccess(credentials: SshCredentials) {
        _loginSuccess.postValue(credentials)
    }

    override fun onLoginError(message: String) {
        Timber.d("Sftp error: $message")
        _loginError.postValue(message)
    }
}