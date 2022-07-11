package io.xxlabs.messenger.backup.cloud.sftp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class SftpAuthViewModel : ViewModel(), SftpLoginListener {

    val sftpLoginUi: LiveData<SftpLoginUi> by ::_sftpLoginUi
    private val _sftpLoginUi = MutableLiveData<SftpLoginUi>(SftpLogin(this))

    val loginSuccess: LiveData<SftpCredentials?> by ::_loginSuccess
    private val _loginSuccess = MutableLiveData<SftpCredentials?>(null)

    override fun onLoginSuccess(credentials: SftpCredentials) {
        _loginSuccess.postValue(credentials)
    }

    override fun onLoginError(message: String) {
        Timber.d("Sftp error: $message")
    }
}