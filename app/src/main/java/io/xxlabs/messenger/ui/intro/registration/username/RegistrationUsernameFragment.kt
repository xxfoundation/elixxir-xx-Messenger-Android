package io.xxlabs.messenger.ui.intro.registration.username

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.FragmentRegistrationUsernameBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.support.dialog.info.InfoDialog
import io.xxlabs.messenger.ui.intro.registration.RegistrationViewModel
import io.xxlabs.messenger.ui.main.ud.registration.UdRegistrationViewModel
import javax.inject.Inject

/**
 * Username creation screen.
 */
class RegistrationUsernameFragment : Fragment(), Injectable {

    /* ViewModels */

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var udProfileRegistrationViewModel: UdRegistrationViewModel

    /* UI */

    private lateinit var binding: FragmentRegistrationUsernameBinding
    private val ui: UsernameRegistrationController by lazy { registrationViewModel }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_registration_username,
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
        binding.ui = registrationViewModel
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        ui.usernameInfoClicked.observe(viewLifecycleOwner) { show ->
            if (show) displayUsernameInfoDialog()
        }

        ui.usernameNavigateNextStep.observe(viewLifecycleOwner) { username ->
            username?.let { navigateNextStep(it) }
        }

        ui.usernameNavigateDemo.observe(viewLifecycleOwner) { isDemoAcct ->
            if (isDemoAcct) navigateDemo()
        }
    }

    private fun displayUsernameInfoDialog() {
        InfoDialog.newInstance(ui.usernameDialogUI)
            .show(requireActivity().supportFragmentManager, null)
        ui.onUsernameInfoHandled()
    }

    private fun navigateNextStep(username: String) {
        val directions = RegistrationUsernameFragmentDirections
            .actionRegistrationUsernameFragmentToRegistrationWelcomeFragment(username)
        findNavController().navigate(directions)
        ui.onUsernameNavigateHandled()
    }

    private fun navigateDemo() {
        val directions = RegistrationUsernameFragmentDirections
            .actionGlobalChats()
        findNavController().navigate(directions)
        ui.onUsernameNavigateHandled()
    }
}