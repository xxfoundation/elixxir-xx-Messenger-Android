package io.xxlabs.messenger.backup.ui.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.cloud.CloudAuthentication
import io.xxlabs.messenger.backup.data.BackupSource
import io.xxlabs.messenger.databinding.FragmentBackupLocationsBinding
import io.xxlabs.messenger.databinding.ListItemBackupLocationBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.support.view.SnackBarActivity
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialog
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialogUI

/**
 * Lists cloud storage services to choose as a backup name.
 */
abstract class BackupLocationsFragment : Fragment(), Injectable {

    /* ViewModels */

    abstract val backupViewModel: BackupLocationsViewModel
    protected lateinit var cloudAuthentication: CloudAuthentication

    /* UI */

    private lateinit var binding: FragmentBackupLocationsBinding
    private val ui: BackupLocationsController by lazy { backupViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cloudAuthentication = CloudAuthentication(requireActivity().activityResultRegistry)
        lifecycle.addObserver(cloudAuthentication)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_backup_locations,
            container,
            false
        )
        binding.ui = backupViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.backupLocationsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = BackupProvidersAdapter(backupViewModel.locations)
        }
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        ui.navigateToDetail.observe(viewLifecycleOwner) { source ->
            source?.let {
                navigateToDetail(source)
                ui.onNavigationHandled()
            }
        }

        ui.authLaunchConsentDialog.observe(viewLifecycleOwner) { dialogUi ->
            dialogUi?.let {
                showConsentDialog(dialogUi)
                ui.onLaunchConsentHandled()
            }
        }

        ui.backupError.observe(viewLifecycleOwner) { error ->
            error?.let {
                (requireActivity() as SnackBarActivity).createSnackMessage(error)
                ui.onErrorHandled()
            }
        }
    }

    protected abstract fun navigateToDetail(source: BackupSource)

    private fun showConsentDialog(dialogUi: TwoButtonInfoDialogUI) {
        TwoButtonInfoDialog.newInstance(dialogUi)
            .show(requireActivity().supportFragmentManager, null)
    }
}

private class BackupProvidersAdapter(
    private val locations: List<LocationOption>
) : RecyclerView.Adapter<BackupProvidersAdapter.BackupProviderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BackupProviderViewHolder.create(parent)

    override fun onBindViewHolder(holder: BackupProviderViewHolder, position: Int) =
        holder.onBind(locations[position])

    override fun getItemCount(): Int = locations.size

    class BackupProviderViewHolder(
        private val binding: ListItemBackupLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(ui: LocationOption) {
            binding.ui = ui
        }

        companion object {
            fun create(parent: ViewGroup) = BackupProviderViewHolder(
                ListItemBackupLocationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
}