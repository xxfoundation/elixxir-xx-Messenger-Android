package io.xxlabs.messenger.backup.cloud.crust.login.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CrustLoginViewModel : ViewModel(), CrustLoginListener {

    val crustLoginUi: LiveData<CrustLoginUi> by ::_crustLoginUi
    private val _crustLoginUi = MutableLiveData<CrustLoginUi>(CrustLogin(this))

    val username: LiveData<String?> by ::_username
    private val _username = MutableLiveData<String?>(null)

    override fun onUsernameSubmitted(username: String) {
        _username.value = username
    }
}
