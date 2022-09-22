package io.xxlabs.messenger.ui.intro.registration

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistration
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistrationFactory
import javax.inject.Inject

abstract class RegistrationFlowFragment : Fragment(), Injectable {

    /* ViewModels */

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected val networkViewModel: NetworkViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory)[NetworkViewModel::class.java]
    }

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

    protected fun onRegistrationComplete() {
        registrationHandler.onRegistrationComplete()
    }
}