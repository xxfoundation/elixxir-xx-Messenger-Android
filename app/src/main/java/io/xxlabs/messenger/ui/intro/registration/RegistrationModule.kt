package io.xxlabs.messenger.ui.intro.registration

import dagger.Binds
import dagger.Module
import io.xxlabs.messenger.ui.intro.registration.added.*
import io.xxlabs.messenger.ui.intro.registration.email.EmailRegistration
import io.xxlabs.messenger.ui.intro.registration.email.EmailRegistrationController
import io.xxlabs.messenger.ui.intro.registration.phone.PhoneRegistration
import io.xxlabs.messenger.ui.intro.registration.phone.PhoneRegistrationController
import io.xxlabs.messenger.ui.intro.registration.tfa.TfaRegistration
import io.xxlabs.messenger.ui.intro.registration.tfa.TfaRegistrationController
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistration
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistrationController
import io.xxlabs.messenger.ui.intro.registration.welcome.WelcomeRegistration
import io.xxlabs.messenger.ui.intro.registration.welcome.WelcomeRegistrationController

@Module
interface RegistrationModule {

    @Binds
    fun usernameStep(registration: UsernameRegistration): UsernameRegistrationController

    @Binds
    fun welcomeStep(registration: WelcomeRegistration): WelcomeRegistrationController

    @Binds
    fun emailStep(registration: EmailRegistration): EmailRegistrationController

    @Binds
    fun phoneStep(registration: PhoneRegistration): PhoneRegistrationController

    @Binds
    fun tfaStep(registration: TfaRegistration): TfaRegistrationController

    @Binds
    fun addedEmailOrPhoneStep(registration: AddedRegistration): AddedRegistrationController
}