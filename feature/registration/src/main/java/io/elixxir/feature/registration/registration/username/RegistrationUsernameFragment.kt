package io.elixxir.feature.registration.registration.username

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import io.elixxir.core.ui.dialog.info.InfoDialog
import io.elixxir.core.ui.util.navigateSafe
import io.elixxir.feature.registration.R
import io.elixxir.feature.registration.databinding.FragmentRegistrationUsernameBinding
import io.elixxir.feature.registration.registration.RegistrationFlowFragment

/**
 * Username creation screen.
 */
class RegistrationUsernameFragment : RegistrationFlowFragment() {

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
        binding.ui = registrationViewModel
    }

    override fun onStart() {
        super.onStart()
        observeState()
    }

    private fun observeState() {
        ui.usernameInfoClicked.observe(viewLifecycleOwner) { show ->
            if (show) displayUsernameInfoDialog()
        }

        ui.usernameNavigateNextStep.observe(viewLifecycleOwner) { username ->
            username?.let { navigateNextStep(it) }
        }

        ui.usernameNavigateDemo.observe(viewLifecycleOwner) { isDemoAcct ->
            if (isDemoAcct) navigateDemo()
        }

        ui.usernameNavigateRestore.observe(viewLifecycleOwner) { restoreAcct ->
            if (restoreAcct) navigateRestore()
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
        navigateSafe(directions)
        ui.onUsernameNavigateHandled()
    }

    private fun navigateDemo() {
        onRegistrationComplete()
        ui.onUsernameNavigateHandled()
    }

    private fun navigateRestore() {
//        val directions = RegistrationUsernameFragmentDirections
//            .actionRegistrationUsernameFragmentToRestoreAccountFragment()
//        navigateSafe(directions)
//        ui.onUsernameNavigateHandled()
    }
}