package io.xxlabs.messenger.ui.intro.registration.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.FragmentRegistrationWelcomeBinding
import io.xxlabs.messenger.ui.dialog.info.InfoDialog
import io.xxlabs.messenger.ui.intro.registration.RegistrationFlowFragment

class RegistrationWelcomeFragment : RegistrationFlowFragment() {

    /* UI */

    private lateinit var binding: FragmentRegistrationWelcomeBinding
    private val ui : WelcomeRegistrationController by lazy { registrationViewModel }
    private val username: String by lazy {
        RegistrationWelcomeFragmentArgs.fromBundle(requireArguments()).username
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_registration_welcome,
            container,
            false
        )
        initViewModels()
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private fun initViewModels() {
        binding.username = username
        binding.ui = registrationViewModel
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        ui.welcomeNavigateNext.observe(viewLifecycleOwner) { next ->
            if (next) navigateNextStep()
        }

        ui.welcomeNavigateSkip.observe(viewLifecycleOwner) { skip ->
            if (skip) navigateToChats()
        }

        ui.welcomeInfoClicked.observe(viewLifecycleOwner) { show ->
            if (show) displayWelcomeInfoDialog()
        }
    }

    private fun displayWelcomeInfoDialog() {
        InfoDialog.newInstance(ui.welcomeDialogUI)
            .show(requireActivity().supportFragmentManager, null)
        ui.onWelcomeInfoHandled()
    }

    private fun navigateNextStep() {
        val directions = RegistrationWelcomeFragmentDirections
            .actionRegistrationWelcomeFragmentToRegistrationEmailFragment()
        findNavController().navigate(directions)
        ui.onWelcomeNavigateHandled()
    }

    private fun navigateToChats() {
        onRegistrationComplete()
        ui.onWelcomeNavigateHandled()
    }
}