package io.xxlabs.messenger.ui.main.contacts.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.databinding.FragmentContactSelectionBinding
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.ui.dialog.info.showInfoDialog
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.main.contacts.ContactsFragment
import io.xxlabs.messenger.ui.main.contacts.list.ConnectionsListScrollHandler
import io.xxlabs.messenger.ui.main.contacts.list.ConnectionsViewModel
import io.xxlabs.messenger.ui.main.contacts.list.ContactListFragmentDirections
import io.xxlabs.messenger.ui.main.groups.create.CreateGroupDialog
import io.xxlabs.messenger.ui.main.groups.create.CreateGroupDialogUI
import javax.inject.Inject

class ContactSelectionFragment : ContactsFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentContactSelectionBinding
    private lateinit var contactsViewModel: ContactsViewModel

    private val connectionsViewModel: ConnectionsViewModel by viewModels { viewModelFactory }
    private val selectionsAdapter: SelectionsAdapter = SelectionsAdapter()

    override val scrollHandler: ConnectionsListScrollHandler by lazy { connectionsViewModel }
    override val connectionsAdapter: SelectableContactsAdapter = SelectableContactsAdapter()
    override val connectionsRecyclerView: RecyclerView get() = binding.connectionsList
    override val lettersScrollbar: View get() = binding.lettersScrollbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactSelectionBinding.inflate(
            inflater,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            ui = connectionsViewModel
            toolbarUi = connectionsViewModel.toolbar
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initContactsViewModel()
        initRecyclerView()
    }

    private fun initContactsViewModel() {
        contactsViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[ContactsViewModel::class.java]
    }

    private fun initRecyclerView() {
        binding.selectedContactsRV.apply {
            adapter = selectionsAdapter
            layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
        }
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private var selectedMembers = listOf<SelectedContactUI>()

    private fun observeUI() {
        connectionsViewModel.createGroupToolbar.observe(viewLifecycleOwner) { toolbar ->
            binding.toolbarUi = toolbar
        }

        connectionsViewModel.selectableContacts.observe(viewLifecycleOwner) { contacts ->
            connectionsAdapter.submitList(contacts)
        }

        connectionsViewModel.selectedContacts.observe(viewLifecycleOwner) { selections ->
            selectedMembers = selections
            selectionsAdapter.submitList(selections)
        }

        connectionsViewModel.createGroupDialog.observe(viewLifecycleOwner) { showDialog ->
            if (showDialog) {
                showCreateGroupDialog()
                connectionsViewModel.onCreateGroupHandled()
            }
        }

        connectionsViewModel.navigateUp.observe(viewLifecycleOwner) { goBack ->
            if (goBack) {
                findNavController().navigateUp()
                connectionsViewModel.onNavigateUpHandled()
            }
        }

        connectionsViewModel.navigateToSearch.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                navigateToSearch()
                connectionsViewModel.onSearchNavigationHandled()
            }
        }

        contactsViewModel.navigateToGroup.observe(viewLifecycleOwner) { groupId ->
            groupId?.let {
                navigateToGroup(it)
                contactsViewModel.onNavigateToGroupHandled()
            }
        }
    }

    private fun showCreateGroupDialog() {
        val ui = CreateGroupDialogUI.create(
            getString(R.string.group_create_dialog_body, selectedMembers.size),
            ::onCreateGroupClicked,
            ::showGroupInfoDialog
        )
        CreateGroupDialog.newInstance(ui)
            .show(requireActivity().supportFragmentManager, null)
    }

    private fun onCreateGroupClicked(name: String?, description: String?) {
        val members = selectedMembers.map { it.contact as ContactData }
        name?.let {
            contactsViewModel.createGroup(name, description, members)
        }
    }

    private fun showGroupInfoDialog() {
        showInfoDialog(
            R.string.group_create_dialog_info_title,
            R.string.group_create_dialog_info_body,
            null
        )
    }

    private fun navigateToSearch() {
        val directions = ContactSelectionFragmentDirections.actionContactsSelectionToSearch()
        findNavController().navigateSafe(directions)
    }


    private fun navigateToGroup(groupId: ByteArray) {
        val directions = ContactSelectionFragmentDirections.actionGlobalGroupsChat().apply {
            this.groupId = groupId.toBase64String()
        }
        findNavController().navigateSafe(directions)
    }
}