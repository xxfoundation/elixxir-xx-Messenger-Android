package io.xxlabs.messenger.ui.intro.registration.username

import android.text.InputFilter
import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface UsernameRegistrationUI {
    val usernameTitle: Spanned
    val username: MutableLiveData<String?>
    val maxUsernameLength: Int
    val usernameError: LiveData<String?>
    val usernameNextButtonEnabled: LiveData<Boolean>
    val usernameInputEnabled: LiveData<Boolean>
    val usernameFilters: Array<InputFilter>
    fun onUsernameInfoClicked()
    fun onUsernameNextClicked()
}