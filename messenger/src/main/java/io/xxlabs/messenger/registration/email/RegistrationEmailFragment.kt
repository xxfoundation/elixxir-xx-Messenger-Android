package io.xxlabs.messenger.ui.intro.registration.email

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.FragmentRegistrationEmailBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.ui.dialog.info.InfoDialog
import io.xxlabs.messenger.ui.intro.registration.tfa.TwoFactorAuthCredentials
import io.xxlabs.messenger.ui.intro.registration.RegistrationFlowFragment

class RegistrationEmailFragment : RegistrationFlowFragment(), Injectable {

    /* UI */

    private lateinit var binding: FragmentRegistrationEmailBinding
    private val ui: EmailRegistrationController by lazy { registrationViewModel }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_registration_email,
            container,
            false
        )
        initViewModels()
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
        ui.emailInfoClicked.observe(viewLifecycleOwner) { show ->
            if (show) displayUsernameInfoDialog()
        }

        ui.emailNavigateNextStep.observe(viewLifecycleOwner) { tfa ->
            tfa?.let { navigateNextStep(it) }
        }

        ui.emailNavigateSkip.observe(viewLifecycleOwner) { skip ->
            if (skip) navigateSkip()
        }
    }

    private fun displayUsernameInfoDialog() {
        InfoDialog.newInstance(ui.emailDialogUI)
            .show(requireActivity().supportFragmentManager, null)
        ui.onEmailInfoHandled()
    }

    private fun navigateNextStep(tfaCredentials: TwoFactorAuthCredentials) {
        val directions = RegistrationEmailFragmentDirections
            .actionRegistrationEmailFragmentToRegistrationTfaFragment(tfaCredentials)
        findNavController().navigate(directions)
        ui.onEmailNavigateHandled()
    }

    private fun navigateSkip() {
        val directions = RegistrationEmailFragmentDirections.
            actionRegistrationEmailFragmentToRegistrationPhoneFragment()
        findNavController().navigate(directions)
        ui.onEmailNavigateHandled()
    }
}