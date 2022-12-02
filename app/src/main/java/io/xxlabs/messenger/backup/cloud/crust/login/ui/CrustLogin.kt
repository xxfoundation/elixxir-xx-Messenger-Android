package io.xxlabs.messenger.backup.cloud.crust.login.ui

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface CrustLoginListener {
    fun onUsernameSubmitted(username: String)
}

class CrustLogin(
    private val listener: CrustLoginListener,
) : CrustLoginUi {

    private var username: String = ""

    override val submitButtonEnabled: LiveData<Boolean> by ::_submitButtonEnabled
    private val _submitButtonEnabled = MutableLiveData(false)

    override val textInputEnabled: LiveData<Boolean> by ::_textInputEnabled
    private val _textInputEnabled = MutableLiveData(true)

    override fun onUsernameInput(text: Editable) {
        username = text.toString()
        maybeEnableSubmitButton()
    }

    override fun onSubmitClicked() {
        listener.onUsernameSubmitted(username)
    }

    private fun maybeEnableSubmitButton() {
        _submitButtonEnabled.value = isFormComplete()
    }

    private fun isFormComplete(): Boolean {
        return username.isNotEmpty()
    }
}