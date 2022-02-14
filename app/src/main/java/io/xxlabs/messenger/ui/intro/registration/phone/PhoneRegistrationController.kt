package io.xxlabs.messenger.ui.intro.registration.phone

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.intro.registration.tfa.TwoFactorAuthCredentials

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