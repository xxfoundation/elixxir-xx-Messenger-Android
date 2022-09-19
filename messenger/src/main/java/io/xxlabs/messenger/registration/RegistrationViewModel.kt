package io.xxlabs.messenger.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.xxlabs.messenger.ui.intro.registration.email.EmailRegistrationController
import io.xxlabs.messenger.ui.intro.registration.phone.PhoneRegistrationController
import io.xxlabs.messenger.ui.intro.registration.success.CompletedRegistrationStepController
import io.xxlabs.messenger.ui.intro.registration.tfa.TfaRegistrationController
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistrationController
import io.xxlabs.messenger.ui.intro.registration.welcome.WelcomeRegistrationController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class RegistrationViewModel (
    welcomeRegistration: WelcomeRegistrationController,
    emailRegistration: EmailRegistrationController,
    phoneRegistration: PhoneRegistrationController,
    completedStep: CompletedRegistrationStepController,
    tfaRegistration: TfaRegistrationController,
    private val usernameRegistration: UsernameRegistrationController
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

    companion object {
        fun provideFactory(
            assistedFactory: RegistrationViewModelFactory,
            usernameRegistration: UsernameRegistrationController
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(usernameRegistration) as T
            }
        }
    }
}

interface RegistrationViewModelFactory {
    fun create(usernameRegistration: UsernameRegistrationController): RegistrationViewModel
}