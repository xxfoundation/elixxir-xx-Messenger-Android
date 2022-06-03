package io.xxlabs.messenger.ui.main.contacts.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.databinding.FragmentContactSelectionBinding
import io.xxlabs.messenger.ui.main.contacts.ContactsFragment
import io.xxlabs.messenger.ui.main.contacts.list.ConnectionsListScrollHandler
import io.xxlabs.messenger.ui.main.contacts.list.ConnectionsViewModel
import javax.inject.Inject

class ContactSelectionFragment : ContactsFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentContactSelectionBinding

    private val connectionsViewModel: ConnectionsViewModel by viewModels { viewModelFactory }
    private val selectionsAdapter: SelectionsAdapter = SelectionsAdapter()

    override val scrollHandler: ConnectionsListScrollHandler by lazy { connectionsViewModel }
    override val connectionsAdapter: SelectableContactsAdapter = SelectableContactsAdapter()
    override val connectionsRecyclerView: RecyclerView by lazy { binding.connectionsList }
    override val lettersScrollbar: View by lazy { binding.lettersScrollbar }

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
        initRecyclerView()
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

    private fun observeUI() {
        connectionsViewModel.selectableContacts.observe(viewLifecycleOwner) { contacts ->
            connectionsAdapter.submitList(contacts)
        }

        connectionsViewModel.selectedContacts.observe(viewLifecycleOwner) { selections ->
            selectionsAdapter.submitList(selections)
        }
    }
}