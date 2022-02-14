package io.xxlabs.messenger.ui.intro.registration.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.FragmentRegistrationWelcomeBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.support.dialog.info.InfoDialog
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.intro.registration.RegistrationViewModel
import io.xxlabs.messenger.ui.main.ud.registration.UdRegistrationViewModel
import javax.inject.Inject

class RegistrationWelcomeFragment : Fragment(), Injectable {

    /* ViewModels */

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var udProfileRegistrationViewModel: UdRegistrationViewModel

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
        registrationViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[RegistrationViewModel::class.java]
        networkViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[NetworkViewModel::class.java]
        udProfileRegistrationViewModel =
            ViewModelProvider(this, viewModelFactory)[UdRegistrationViewModel::class.java]

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
        InfoDialog(ui.welcomeDialogUI).show(requireActivity().supportFragmentManager, null)
        ui.onWelcomeInfoHandled()
    }

    private fun navigateNextStep() {
        val directions = RegistrationWelcomeFragmentDirections
            .actionRegistrationWelcomeFragmentToRegistrationEmailFragment()
        findNavController().navigate(directions)
        ui.onWelcomeNavigateHandled()
    }

    private fun navigateToChats() {
        val directions = RegistrationWelcomeFragmentDirections.actionGlobalChats()
        findNavController().navigate(directions)
        ui.onWelcomeNavigateHandled()
    }
}