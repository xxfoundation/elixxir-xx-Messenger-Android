package io.xxlabs.messenger.ui.main.contacts

import android.os.Bundle
import android.text.method.Touch.scrollTo
import android.view.LayoutInflater
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.databinding.FragmentConnectionsBinding
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.main.contacts.list.ConnectionsAdapter
import io.xxlabs.messenger.ui.main.contacts.list.ConnectionsViewModel
import timber.log.Timber
import javax.inject.Inject

class ContactsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val connectionsViewModel: ConnectionsViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentConnectionsBinding
    private val connectionsAdapter: ConnectionsAdapter = ConnectionsAdapter()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.connectionsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = connectionsAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        detectScrollGesture()
        observeUI()
    }

    private fun detectScrollGesture() {
        binding.lettersScrollbar.apply {
            setOnTouchListener { view, motionEvent ->
                view.performClick()
                when (motionEvent.action) {
                    ACTION_MOVE -> {
                        connectionsViewModel.onLettersScrolled(top, bottom, motionEvent.y)
                        true
                    }
                    ACTION_UP -> {
                        connectionsViewModel.onScrollStopped()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun observeUI() {
        connectionsViewModel.connectionsList.observe(viewLifecycleOwner) { connections ->
            connectionsAdapter.submitList(connections)
        }

        connectionsViewModel.scrollToPosition.observe(viewLifecycleOwner) { position ->
            position?.let { scrollToConnectionListPosition(it) }
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

    private fun scrollToConnectionListPosition(position: Int) {
        try {
            binding.connectionsList.smoothScrollToPosition(position)
        } catch (e: Exception) {
            Timber.d("An exception was thrown: ${e.message}")
        }
    }

    private fun navigateToPrivateChat(contact: Contact) {
        val directions = ContactsFragmentDirections.actionGlobalChat().apply {
            this.contact = contact as ContactData
            contactId = contact.userId.toBase64String()
        }
        findNavController().navigateSafe(directions)
    }

    private fun navigateToGroupChat(group: Group) {
        val directions = ContactsFragmentDirections.actionGlobalGroupsChat()
            .apply {
            this.group = group as GroupData
            groupId = group.groupId.toBase64String()
        }
        findNavController().navigateSafe(directions)
    }

    private fun navigateToSearch() {
        val directions = ContactsFragmentDirections.actionContactsToSearch()
        findNavController().navigateSafe(directions)
    }

    private fun navigateToContactSelection() {
        // TODO: Navigate to select contact screen
    }
}