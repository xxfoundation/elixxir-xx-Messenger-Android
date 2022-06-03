package io.xxlabs.messenger.ui.main.contacts.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.databinding.FragmentConnectionsBinding
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.ui.main.contacts.ContactsFragment
import javax.inject.Inject

class ContactListFragment : ContactsFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val connectionsViewModel: ConnectionsViewModel by viewModels { viewModelFactory }
    override val scrollHandler: ConnectionsListScrollHandler by lazy { connectionsViewModel }

    private lateinit var binding: FragmentConnectionsBinding
    override val connectionsAdapter: ConnectionsAdapter = ConnectionsAdapter()
    override val connectionsRecyclerView: RecyclerView by lazy { binding.connectionsList }
    override val lettersScrollbar: View by lazy { binding.lettersScrollbar }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConnectionsBinding.inflate(
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

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        connectionsViewModel.connectionsList.observe(viewLifecycleOwner) { connections ->
            connectionsAdapter.submitList(connections)
        }

        connectionsViewModel.navigateToChat.observe(viewLifecycleOwner) { contact ->
            contact?.let {
                navigateToPrivateChat(contact)
                connectionsViewModel.onNavigateToChatHandled()
            }
        }

        connectionsViewModel.navigateToGroup.observe(viewLifecycleOwner) { group ->
            group?.let {
                navigateToGroupChat(group)
                connectionsViewModel.onNavigateToGroupHandled()
            }
        }

        connectionsViewModel.navigateToSearch.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                navigateToSearch()
                connectionsViewModel.onSearchNavigationHandled()
            }
        }

        connectionsViewModel.navigateToContactSelection.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                navigateToContactSelection()
                connectionsViewModel.onContactSelectionNavigationHandled()
            }
        }

        connectionsViewModel.navigateUp.observe(viewLifecycleOwner) { goBack ->
            if (goBack) {
                findNavController().navigateUp()
                connectionsViewModel.onNavigateUpHandled()
            }
        }
    }

    private fun navigateToPrivateChat(contact: Contact) {
        val directions = ContactListFragmentDirections.actionGlobalChat()
            .apply {
            this.contact = contact as ContactData
            contactId = contact.userId.toBase64String()
        }
        findNavController().navigateSafe(directions)
    }

    private fun navigateToGroupChat(group: Group) {
        val directions = ContactListFragmentDirections.actionGlobalGroupsChat()
            .apply {
            this.group = group as GroupData
            groupId = group.groupId.toBase64String()
        }
        findNavController().navigateSafe(directions)
    }

    private fun navigateToSearch() {
        val directions = ContactListFragmentDirections.actionContactsToSearch()
        findNavController().navigateSafe(directions)
    }

    private fun navigateToContactSelection() {
        val directions = ContactListFragmentDirections.actionContactsToContactsSelect()
        findNavController().navigateSafe(directions)
    }
}