package io.xxlabs.messenger.backup.ui.save

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.backup.auth.CloudAuthentication
import io.xxlabs.messenger.backup.model.BackupOption
import io.xxlabs.messenger.backup.ui.list.BackupListFragmentDirections
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
    private val backupOptionsAdapter by lazy { BackupOptionsAdapter() }

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
        binding.ui = ui
        binding.lifecycleOwner = this
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

        ui.backupError.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
            }
        }

        ui.navigateToDetail.observe(viewLifecycleOwner) { option ->
            option?.let {
                onNavigateToDetail(option)
            }
        }
    }

    private fun showInfoDialog() {
        ui.onInfoDialogHandled()
    }

    private fun showError(error: String) {
        (requireActivity() as SnackBarActivity).createSnackMessage(error)
        ui.onErrorHandled()
    }

    private fun onNavigateToDetail(backup: BackupOption) {
        val directions = BackupSettingsFragmentDirections
            .actionBackupSettingsToBackupDetail(backup)
        findNavController().navigate(directions)
        ui.onNavigationHandled()
    }
}

private class BackupOptionsAdapter : RecyclerView.Adapter<BackupOptionsAdapter.BackupOptionViewHolder>() {

    private var locations: List<SettingsOption> = listOf()

    fun refreshList(backupOptions: List<SettingsOption>) {
        locations = backupOptions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BackupOptionViewHolder.create(parent)

    override fun onBindViewHolder(holder: BackupOptionViewHolder, position: Int) =
        holder.onBind(locations[position])

    override fun getItemCount(): Int = locations.size

    class BackupOptionViewHolder(
        private val binding: ListItemBackupOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(ui: SettingsOption) {
            binding.ui = ui
        }

        companion object {
            fun create(parent: ViewGroup) = BackupOptionViewHolder(
                ListItemBackupOptionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
}