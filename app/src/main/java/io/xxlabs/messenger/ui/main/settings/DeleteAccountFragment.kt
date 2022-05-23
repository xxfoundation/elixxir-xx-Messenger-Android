package io.xxlabs.messenger.ui.main.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.FragmentDeleteAccountBinding
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.extensions.toast
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.dialog.info.showInfoDialog
import io.xxlabs.messenger.ui.intro.splash.RegistrationIntroActivity
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import javax.inject.Inject

/**
 * Account deletion confirmation screen.
 */
class DeleteAccountFragment : BaseFragment() {

    /* ViewModels */

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var settingsViewModel: SettingsViewModel

    /* UI */

    private lateinit var binding: FragmentDeleteAccountBinding
    private val ui: DeleteAccountUIController by lazy { settingsViewModel }
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_delete_account,
            container,
            false
        )

        initViewModels()
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
    }

    private fun initToolbar() {
        binding.toolbar.root.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
        binding.toolbar.toolbarGenericTitle.text = ""

        toolbarGenericBackBtn.setOnClickListener {
            cancel()
        }
    }

    private fun initViewModels() {
        settingsViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[SettingsViewModel::class.java]
        binding.ui = settingsViewModel
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        ui.infoClicked.observe(viewLifecycleOwner) { showInfo ->
            if (showInfo) {
                displayDeleteAccountInfo()
                ui.onInfoHandled()
            }
        }

        ui.accountDeleted.observe(viewLifecycleOwner) { deleted ->
            if (deleted) {
                navigateToIntroScreen()
                ui.onAccountDeletedHandled()
            }
        }

        ui.deletionError.observe(viewLifecycleOwner) { error ->
            error?.let { showError(it) }
        }

        ui.cancelClicked.observe(viewLifecycleOwner) { cancelled ->
            if (cancelled) {
                cancel()
                ui.onCancelHandled()
            }
        }

        ui.loading.observe(viewLifecycleOwner) {
            isLoading = it
        }
    }

    private fun displayDeleteAccountInfo() {
        showInfoDialog(
            R.string.settings_confirm_delete_account_info_dialog_title,
            R.string.settings_confirm_delete_account_info_dialog_body
        )
    }

    private fun cancel() {
        if (!isLoading) findNavController().navigateUp()
        else requireContext().toast("Deletion has started. This cannot be cancelled.")
    }

    private fun navigateToIntroScreen() {
        val introIntent = Intent(
            requireActivity(),
            RegistrationIntroActivity::class.java)
        startActivity(introIntent)
        requireActivity().finishAfterTransition()
    }
}