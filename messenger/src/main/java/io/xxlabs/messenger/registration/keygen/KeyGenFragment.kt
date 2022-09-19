package io.xxlabs.messenger.registration.keygen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import io.xxlabs.messenger.databinding.FragmentKeygenBinding
import io.xxlabs.messenger.dialog.warning.WarningDialog
import io.xxlabs.messenger.dialog.warning.WarningDialogUi
import io.xxlabs.messenger.util.navigateSafe
import kotlinx.coroutines.flow.collect

class KeyGenFragment : Fragment() {

    private lateinit var binding: FragmentKeygenBinding
    private val viewModel: KeyGenViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lifecycleScope.launchWhenStarted {
            observeEvents()
        }

        binding = FragmentKeygenBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private suspend fun observeEvents() {
        viewModel.keyGenerationComplete.collect { complete ->
            if (complete) {
                continueRegistration()
            }
        }

        viewModel.keyGenError.collect { error ->
            error?.let {
                displayError(it)
            }
        }
    }

    private fun continueRegistration() {
        navigateSafe(
            KeyGenFragmentDirections.actionKeyGenFragmentToRegistrationUsernameFragment()
        )
    }

    private fun displayError(errorUi: WarningDialogUi) {
        WarningDialog.newInstance(errorUi).show(childFragmentManager, null)
    }
}