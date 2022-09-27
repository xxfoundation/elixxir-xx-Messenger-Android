package io.elixxir.feature.registration.registration

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.xxlabs.messenger.ui.intro.registration.email.EmailRegistrationController
import io.xxlabs.messenger.ui.intro.registration.phone.PhoneRegistrationController
import io.xxlabs.messenger.ui.intro.registration.success.CompletedRegistrationStepController
import io.xxlabs.messenger.ui.intro.registration.tfa.TfaRegistrationController
import io.elixxir.feature.registration.registration.username.UsernameRegistrationController
import io.xxlabs.messenger.ui.intro.registration.welcome.WelcomeRegistrationController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    usernameRegistration: UsernameRegistrationController,
    welcomeRegistration: WelcomeRegistrationController,
    emailRegistration: EmailRegistrationController,
    phoneRegistration: PhoneRegistrationController,
    completedStep: CompletedRegistrationStepController,
    tfaRegistration: TfaRegistrationController,
) : ViewModel(),
    UsernameRegistrationController by usernameRegistration,
    WelcomeRegistrationController by welcomeRegistration,
    EmailRegistrationController by emailRegistration,
    PhoneRegistrationController by phoneRegistration,
    CompletedRegistrationStepController by completedStep,
    TfaRegistrationController by tfaRegistration
{

    val registrationComplete: Flow<Boolean> by ::_registrationComplete
    private val _registrationComplete = MutableStateFlow(false)
}