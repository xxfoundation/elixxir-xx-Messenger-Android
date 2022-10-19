package io.xxlabs.messenger.ui.intro.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.ui.intro.registration.email.EmailRegistrationController
import io.xxlabs.messenger.ui.intro.registration.phone.PhoneRegistrationController
import io.xxlabs.messenger.ui.intro.registration.success.CompletedRegistrationStepController
import io.xxlabs.messenger.ui.intro.registration.tfa.TfaRegistrationController
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistrationController
import io.xxlabs.messenger.ui.intro.registration.welcome.WelcomeRegistrationController

class RegistrationViewModel @AssistedInject constructor(
    welcomeRegistration: WelcomeRegistrationController,
    emailRegistration: EmailRegistrationController,
    phoneRegistration: PhoneRegistrationController,
    completedStep: CompletedRegistrationStepController,
    tfaRegistration: TfaRegistrationController,
    usernameRegistration: UsernameRegistrationController
) : ViewModel(),
    UsernameRegistrationController by usernameRegistration,
    WelcomeRegistrationController by welcomeRegistration,
    EmailRegistrationController by emailRegistration,
    PhoneRegistrationController by phoneRegistration,
    CompletedRegistrationStepController by completedStep,
    TfaRegistrationController by tfaRegistration
{

    companion object {
        fun provideFactory(
            assistedFactory: RegistrationViewModelFactory,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create() as T
            }
        }
    }
}

@AssistedFactory
interface RegistrationViewModelFactory {
    fun create(): RegistrationViewModel
}