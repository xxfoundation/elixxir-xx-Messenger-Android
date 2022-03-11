package io.xxlabs.messenger.ui.intro.registration

import androidx.lifecycle.ViewModel
import io.xxlabs.messenger.ui.intro.registration.added.AddedRegistrationController
import io.xxlabs.messenger.ui.intro.registration.email.EmailRegistrationController
import io.xxlabs.messenger.ui.intro.registration.phone.PhoneRegistrationController
import io.xxlabs.messenger.ui.intro.registration.tfa.TfaRegistrationController
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistrationController
import io.xxlabs.messenger.ui.intro.registration.welcome.WelcomeRegistrationController
import javax.inject.Inject

class RegistrationViewModel @Inject constructor(
    usernameRegistration: UsernameRegistrationController,
    welcomeRegistration: WelcomeRegistrationController,
    emailRegistration: EmailRegistrationController,
    phoneRegistration: PhoneRegistrationController,
    addedRegistration: AddedRegistrationController,
    tfaRegistration: TfaRegistrationController,
) : ViewModel(),
    UsernameRegistrationController by usernameRegistration,
    WelcomeRegistrationController by welcomeRegistration,
    EmailRegistrationController by emailRegistration,
    PhoneRegistrationController by phoneRegistration,
    AddedRegistrationController by addedRegistration,
    TfaRegistrationController by tfaRegistration