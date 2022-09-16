package io.xxlabs.messenger.registration

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistration
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistrationFactory
import java.util.logging.Logger.global
import javax.inject.Inject

abstract class RegistrationFlowFragment : Fragment() {

    /* ViewModels */

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var usernameRegistrationFactory: UsernameRegistrationFactory
    private val usernameRegistration: UsernameRegistration by lazy {
        UsernameRegistration.provideFactory(
            usernameRegistrationFactory,
            registrationHandler.rsaDecryptPwd(),
            networkViewModel
        )
    }

    @Inject
    lateinit var registrationViewModelFactory: RegistrationViewModelFactory
    protected val registrationViewModel: RegistrationViewModel by activityViewModels {
        RegistrationViewModel.provideFactory(registrationViewModelFactory, usernameRegistration)
    }

    /* UI */

    private lateinit var registrationHandler: RegistrationHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as? RegistrationHandler)?.run {
            registrationHandler = this
        } ?: throw Exception("Activity must implement RegistrationHandler!")
    }
}