package io.xxlabs.messenger.ui.intro.registration.phone

import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface PhoneRegistrationUI {
    val phoneTitle: Spanned
    var phone: MutableLiveData<String?>
    val dialCodeUI: LiveData<String>
    val maxPhoneLength: Int
    val phoneError: LiveData<String?>
    val phoneNextButtonEnabled: LiveData<Boolean>
    val phoneSkipButtonEnabled: LiveData<Boolean>
    val phoneInputEnabled: LiveData<Boolean>
    fun onCountryCodeClicked()
    fun onPhoneInfoClicked()
    fun onPhoneNextClicked()
    fun onPhoneSkipClicked()
}