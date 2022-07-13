package io.xxlabs.messenger.backup.cloud.sftp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class SshAuthViewModel : ViewModel(), SshLoginListener {

    val sftpLoginUi: LiveData<SftpLoginUi> by ::_sftpLoginUi
    private val _sftpLoginUi = MutableLiveData<SftpLoginUi>(SftpLogin(this))

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