package io.xxlabs.messenger.backup.ui.backup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.backup.cloud.CloudAuthentication
import io.xxlabs.messenger.backup.data.BackupSource
import io.xxlabs.messenger.backup.ui.dialog.TextInputDialogUI
import io.xxlabs.messenger.backup.ui.dialog.TextInputDialog
import io.xxlabs.messenger.databinding.FragmentBackupSettingsBinding
import io.xxlabs.messenger.databinding.ListItemBackupOptionBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.support.view.SnackBarActivity
import javax.inject.Inject

/**
 * Displays a list of [BackupOption]s and shows which one is currently set
 */
class BackupSettingsFragment : Fragment(), Injectable {

    private lateinit var cloudAuthentication: CloudAuthentication

    /* ViewModel */

    @Inject
    lateinit var backupSettingsViewModelFactory: BackupSettingsViewModelFactory
    private val backupViewModel: BackupSettingsViewModel by viewModels {
        BackupSettingsViewModel.provideFactory(
            backupSettingsViewModelFactory,
            cloudAuthentication
        )
    }

    /* UI */

    private lateinit var binding: FragmentBackupSettingsBinding
    private val ui: BackupSettingsController by lazy { backupViewModel }
    private val backupOptionsAdapter by lazy { BackupOptionsAdapter(viewLifecycleOwner) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cloudAuthentication = CloudAuthentication(requireActivity().activityResultRegistry)
        lifecycle.addObserver(cloudAuthentication)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBackupSettingsBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.ui = ui
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }


    private fun initRecyclerView() {
        binding.backupLocationsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = backupOptionsAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        refreshList()
        observeUI()
    }

    private fun refreshList() = backupOptionsAdapter.refreshList(ui.locations)

    private fun observeUI() {
        ui.showInfoDialog.observe(viewLifecycleOwner) { show ->
            if (show) showInfoDialog()
        }

        ui.showSetPasswordPrompt.observe(viewLifecycleOwner) { dialogUI ->
            dialogUI?.let { showSetPasswordDialog(it) }
        }

        ui.backupError.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
            }
        }

        ui.navigateToDetail.observe(viewLifecycleOwner) { backup ->
            backup?.let {
                onNavigateToDetail(backup)
            }
        }
    }

    private fun showInfoDialog() {
        ui.onInfoDialogHandled()
    }

    private fun showSetPasswordDialog(dialogUI: TextInputDialogUI) {
        TextInputDialog.newInstance(dialogUI)
            .show(childFragmentManager, null)
        ui.onPasswordPromptHandled()
    }

    private fun showError(error: String) {
        (requireActivity() as SnackBarActivity).createSnackMessage(error)
        ui.onErrorHandled()
    }

    private fun onNavigateToDetail(source: BackupSource) {
        val directions = BackupSettingsFragmentDirections
            .actionBackupSettingsToBackupDetail(source)
        findNavController().navigate(directions)
        ui.onNavigationHandled()
    }
}

private class BackupOptionsAdapter(
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<BackupOptionsAdapter.BackupOptionViewHolder>() {

    private var locations: List<SettingsOption> = listOf()

    fun refreshList(backupOptions: List<SettingsOption>) {
        locations = backupOptions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BackupOptionViewHolder.create(parent, lifecycleOwner)

    override fun onBindViewHolder(holder: BackupOptionViewHolder, position: Int) =
        holder.onBind(locations[position])

    override fun getItemCount(): Int = locations.size

    class BackupOptionViewHolder(
        private val binding: ListItemBackupOptionBinding,
        private val lifecycleOwner: LifecycleOwner
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(ui: SettingsOption) {
            binding.lifecycleOwner = lifecycleOwner
            binding.ui = ui
        }

        companion object {
            fun create(parent: ViewGroup, lifecycleOwner: LifecycleOwner) =
                BackupOptionViewHolder(
                    ListItemBackupOptionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    lifecycleOwner
                )
        }
    }
}