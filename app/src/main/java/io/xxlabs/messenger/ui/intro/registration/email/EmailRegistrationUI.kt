package io.xxlabs.messenger.ui.intro.registration.email

import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface EmailRegistrationUI {
    val emailTitle: Spanned
    val email: MutableLiveData<String?>
    val maxEmailLength: Int
    val emailError: LiveData<String?>
    val emailNextButtonEnabled: LiveData<Boolean>
    val emailSkipButtonEnabled: LiveData<Boolean>
    val emailInputEnabled: LiveData<Boolean>
    fun onEmailInfoClicked()
    fun onEmailNextClicked()
    fun onEmailSkipClicked()
}