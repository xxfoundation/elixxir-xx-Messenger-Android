package io.xxlabs.messenger.ui.main.contacts.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.NetworkState
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.extensions.incognito
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.contacts.ContactsListAdapter
import io.xxlabs.messenger.ui.main.contacts.ContactsViewHolder
import io.xxlabs.messenger.ui.main.contacts.GroupsSelectionListener
import kotlinx.android.synthetic.main.component_network_error_banner.*
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import timber.log.Timber
import javax.inject.Inject

class ContactSelectionFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var networkViewModel: NetworkViewModel
    lateinit var contactsViewModel: ContactsViewModel

    lateinit var contactsAdapter: ContactsListAdapter
    lateinit var navController: NavController

    private var chooseModeCount = 0
    private var groupsListener = object : GroupsSelectionListener {
        override fun onAddMember(contact: ContactData) {
            chooseModeCount++
            toolbarGenericSubtitle.text = "($chooseModeCount/11)"
            //groupAdapter.addMember(contact)
        }

        override fun onRemoveMember(viewHolder: ContactsViewHolder?, contact: ContactData, pos: Int) {

        }

        override fun onRemoveMember(contact: ContactData, pos: Int) {

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment with the ProductGrid theme
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(ContactsViewModel::class.java)

        networkViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(NetworkViewModel::class.java)

        initComponents(view)
        watchForEvents()
    }

    fun initComponents(root: View) {
        toolbarGeneric.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
        toolbarGenericTitle.text = "Select a Contact"

        startContactLists()

        toolbarGenericBackBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        contactsNewGroup.visibility = View.GONE
        contactsRequests.visibility = View.GONE
        contactRequestsCount.visibility = View.GONE

        contactsSearchBar.doOnTextChanged { text, _, _, _ ->
            contactsAdapter.isSearching = !text.isNullOrEmpty()
            contactsAdapter.filter.filter(text)
        }

        contactsAddContactBtn.setOnSingleClickListener {
            openAddContact()
        }

        contactsSearchBar.incognito(preferences.isIncognitoKeyboardEnabled)
    }

    private fun openAddContact() {
        navController.navigateSafe(R.id.action_contacts_selection_to_ud_search_fragment)
    }

    private fun startContactLists() {
        contactsAdapter = ContactsListAdapter(true, groupsListener)

        contactsRecyclerView.layoutManager = LinearLayoutManager(context)
        contactsRecyclerView.adapter = contactsAdapter
        contactsRecyclerView.itemAnimator = null
    }

    private fun watchForEvents() {
        contactsViewModel.contactsData.observe(viewLifecycleOwner, {
            Timber.v("[CONTACTS LIST] Contacts updated")
        })

        contactsViewModel.groupsData.observe(viewLifecycleOwner, {
            Timber.v("[CONTACTS LIST] Groups updated")
        })

//        contactsViewModel.combinedContactGroupsData.observe(viewLifecycleOwner, { combinedList ->
//            val contactsList =
//                combinedList.first.filter { item ->
//                    item.status == RequestStatus.ACCEPTED.value
//                }
//
//            val groupsList =
//                combinedList.second.filter { item ->
//                    item.status == RequestStatus.ACCEPTED.value
//                }
//
//            val combinedContactGroups = mutableListOf<Any>()
//            combinedContactGroups.addAll(contactsList)
//            combinedContactGroups.addAll(groupsList)
//
//            contactsAdapter.update(combinedContactGroups)
//            checkListIsEmpty()
//        })

        contactsViewModel.contactsData.observe(viewLifecycleOwner) { contactsList ->
            val contacts = contactsList.filter { it.status == RequestStatus.ACCEPTED.value }
            contactsAdapter.update(contacts)
            checkListIsEmpty()
        }

        networkViewModel.networkState.observe(
            viewLifecycleOwner,
            Observer<NetworkState> { networkState ->
                Timber.v("Network State: $networkState")
                if (networkState == NetworkState.HAS_CONNECTION) {
                    networkStatusLayout?.visibility = View.GONE
                } else {
                    val bannerMsg = networkViewModel.getNetworkStateMessage(networkState)
                    networkStatusLayout?.visibility = View.VISIBLE
                    networkStatusText?.text = bannerMsg
                }
            })
        Timber.v("Successful listening to banner updates")
    }

    private fun checkListIsEmpty() {
        if (contactsAdapter.itemCount == 0) {
            contactsRecyclerView?.visibility = View.GONE
            contactsEmptyTitle?.visibility = View.VISIBLE
            contactsAddContactBtn?.visibility = View.VISIBLE
        } else {
            contactsRecyclerView?.visibility = View.VISIBLE
            contactsEmptyTitle?.visibility = View.INVISIBLE
            contactsAddContactBtn?.visibility = View.GONE
        }
    }
}