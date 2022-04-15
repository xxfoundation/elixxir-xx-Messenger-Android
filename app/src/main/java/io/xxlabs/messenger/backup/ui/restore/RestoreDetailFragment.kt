package io.xxlabs.messenger.backup.ui.restore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.FragmentRestoreDetailBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.extensions.toast
import io.xxlabs.messenger.ui.ConfirmDialogLauncher
import io.xxlabs.messenger.ui.base.BaseKeystoreActivity
import io.xxlabs.messenger.ui.intro.registration.success.RegistrationStep
import javax.inject.Inject

class RestoreDetailFragment : Fragment(), Injectable {

    /* ViewModels */

    @Inject
    lateinit var viewModelFactory: BackupFoundViewModelFactory
    private val restoreViewModel: RestoreDetailViewModel by viewModels {
        RestoreDetailViewModel.provideFactory(
            viewModelFactory,
            RestoreDetailFragmentArgs.fromBundle(requireArguments()).restoreOption,
            (requireActivity() as BaseKeystoreActivity).rsaDecryptPwd()
        )
    }

    /* UI */

    private lateinit var binding: FragmentRestoreDetailBinding
    private val ui: RestoreDetailController by lazy { restoreViewModel }
    private val warningLauncher: ConfirmDialogLauncher by lazy {
        ConfirmDialogLauncher(requireActivity().supportFragmentManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_restore_detail,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ui = restoreViewModel
        showMultiDeviceWarning()
    }

    private fun onConfirmButtonClicked() {}
    private fun onConfirmDialogDismissed() {}

    private fun showConfirmDialog(
        title: Int,
        body: Int,
        button: Int,
        action: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        warningLauncher.showConfirmDialog(title, body, button, action, onDismiss)
    }

    private fun showMultiDeviceWarning() {
        showConfirmDialog(
            R.string.backup_restore_multidevice_warning_dialog_title,
            R.string.backup_restore_multidevice_warning_dialog_body,
            R.string.backup_restore_multidevice_warning_dialog_button,
            ::onConfirmButtonClicked,
            ::onConfirmDialogDismissed
        )
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        ui.state.observe(viewLifecycleOwner) { state ->
            updateState(state)
        }

        ui.restoreComplete.observe(viewLifecycleOwner) { navigate ->
            if (navigate) navigateToComplete()
        }

        ui.cancelRestore.observe(viewLifecycleOwner) { cancel ->
            if (cancel) navigateBack()
        }

        ui.restoreError.observe(viewLifecycleOwner) { error ->
            error?.let {
                requireContext().toast(error)
                ui.onErrorHandled()
            }
        }
    }

    private fun updateState(state: RestoreState) {
        childFragmentManager.beginTransaction().replace(
            binding.restoreStateContainer.id,
            RestoreStateFragment.newInstance(state),
            state.toString()
        ).commitAllowingStateLoss()
    }

    private fun navigateToComplete() {
        val directions = RestoreDetailFragmentDirections
            .actionRestoreDetailToRegistrationCompleted(RegistrationStep.RESTORE)
        findNavController().run {
            currentDestination?.getAction(directions.actionId)?.let {
                navigate(directions)
            }
        }
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }
}