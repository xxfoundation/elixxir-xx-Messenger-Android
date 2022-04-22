package io.xxlabs.messenger.ui.intro.registration.phone

import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.intro.registration.tfa.TwoFactorAuthCredentials

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

interface PhoneRegistrationController : PhoneRegistrationUI {
    val phoneDialogUI: InfoDialogUI
    val phoneCountryCodeClicked: LiveData<Boolean>
    val phoneInfoClicked: LiveData<Boolean>
    val phoneNavigateNextStep: LiveData<TwoFactorAuthCredentials?>
    val phoneNavigateSkip: LiveData<Boolean>
    fun onPhoneCountryCodeSelected(selectedCountry: Country?)
    fun onPhoneInfoHandled()
    fun onPhoneNavigateHandled()
}