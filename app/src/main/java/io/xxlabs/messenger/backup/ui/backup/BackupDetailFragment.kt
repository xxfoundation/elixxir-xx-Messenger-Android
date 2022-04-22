package io.xxlabs.messenger.backup.ui.backup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.ui.dialog.TextInputDialogUI
import io.xxlabs.messenger.backup.ui.dialog.TextInputDialog
import io.xxlabs.messenger.backup.ui.dialog.RadioButtonDialog
import io.xxlabs.messenger.backup.ui.dialog.RadioButtonDialogUI
import io.xxlabs.messenger.databinding.FragmentBackupDetailBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.support.extensions.toast
import javax.inject.Inject

/**
 * Screen to modify backup frequency, network, etc.
 */
class BackupDetailFragment : Fragment(), Injectable {

    /* ViewModel */

    @Inject
    lateinit var viewModelFactory: BackupDetailViewModelFactory
    private val backupViewModel: BackupDetailViewModel by viewModels {
        BackupDetailViewModel.provideFactory(
            viewModelFactory,
            BackupDetailFragmentArgs.fromBundle(requireArguments()).backup
        )
    }

    /* UI */

    private lateinit var binding: FragmentBackupDetailBinding
    private val ui: BackupDetailController by lazy { backupViewModel }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_backup_detail,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ui = backupViewModel
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        ui.showInfoDialog.observe(viewLifecycleOwner) { show ->
            if (show) showInfoDialog()
        }

        ui.showFrequencyOptions.observe(viewLifecycleOwner) { dialogUI ->
            dialogUI?.let { showFrequencyDialog(it) }
        }

        ui.showNetworkOptions.observe(viewLifecycleOwner) { dialogUI ->
            dialogUI?.let { showNetworkDialog(it) }
        }

        ui.showSetPasswordPrompt.observe(viewLifecycleOwner) { dialogUI ->
            dialogUI?.let { showSetPasswordDialog(it) }
        }

        ui.backupError.observe(viewLifecycleOwner) { error ->
            error?.message?.let {
                requireContext().toast(it)
            }
        }
    }

    private fun showInfoDialog() {
        ui.onInfoDialogHandled()
    }

    private fun showFrequencyDialog(dialogUI: RadioButtonDialogUI) {
        showRadioButtonDialog(dialogUI)
        ui.onFrequencyOptionsHandled()
    }

    private fun showNetworkDialog(dialogUI: RadioButtonDialogUI) {
        showRadioButtonDialog(dialogUI)
        ui.onNetworkOptionsHandled()
    }

    private fun showRadioButtonDialog(dialogUI: RadioButtonDialogUI) {
        RadioButtonDialog.newInstance(dialogUI)
            .show(childFragmentManager, null)
    }

    private fun showSetPasswordDialog(dialogUI: TextInputDialogUI) {
        TextInputDialog.newInstance(dialogUI)
            .show(childFragmentManager, null)
        ui.onPasswordPromptHandled()
    }
}