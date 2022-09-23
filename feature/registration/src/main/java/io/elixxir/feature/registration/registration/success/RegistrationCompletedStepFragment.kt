package io.xxlabs.messenger.ui.intro.registration.success

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.FragmentRegistrationCompletedStepBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.ui.intro.registration.RegistrationFlowFragment
import io.xxlabs.messenger.ui.intro.registration.success.RegistrationStep.*

class RegistrationCompletedStepFragment : RegistrationFlowFragment(), Injectable {

    /* UI */

    private lateinit var binding: FragmentRegistrationCompletedStepBinding
    private val ui : CompletedRegistrationStepController by lazy { registrationViewModel }
    private val step: RegistrationStep by lazy {
        RegistrationCompletedStepFragmentArgs.fromBundle(requireArguments()).step
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_registration_completed_step,
            container,
            false
        )
        initViewModels()

        binding.step = step
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private fun initViewModels() {
        binding.ui = registrationViewModel
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        ui.onCompletedStepNavigate(step).observe(viewLifecycleOwner) {
            if (it) {
                when (step) {
                    EMAIL -> navigateNextStep()
                    PHONE -> registrationComplete()
                    RESTORE -> restartApp()
                }
            }
        }
    }

    private fun navigateNextStep() {
        val directions = RegistrationCompletedStepFragmentDirections
            .actionRegistrationCompletedStepFragmentToRegistrationPhoneFragment()
        findNavController().navigate(directions)
        ui.onCompletedStepNavigateHandled(step)
    }

    private fun registrationComplete() {
        onRegistrationComplete()
        ui.onCompletedStepNavigateHandled(step)
    }

    private fun restartApp() {
        with (appContext()) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            val mainIntent = Intent.makeRestartActivityTask(launchIntent?.component)
            startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }
    }
}