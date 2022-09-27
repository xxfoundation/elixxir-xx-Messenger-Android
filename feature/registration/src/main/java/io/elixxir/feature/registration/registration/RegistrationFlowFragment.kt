package io.elixxir.feature.registration.registration

import android.content.Context
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class RegistrationFlowFragment : Fragment() {

    /* ViewModels */

    @Inject
    lateinit var registrationViewModel: RegistrationViewModel

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