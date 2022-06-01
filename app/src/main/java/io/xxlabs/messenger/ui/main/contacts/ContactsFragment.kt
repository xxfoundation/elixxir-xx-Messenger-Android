package io.xxlabs.messenger.ui.main.contacts


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.NetworkState
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialog
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.support.view.LooperCircularProgressBar
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.dialog.info.showInfoDialog
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.MainActivity
import io.xxlabs.messenger.ui.main.groups.create.CreateGroupDialog
import io.xxlabs.messenger.ui.main.groups.create.CreateGroupDialogUI
import kotlinx.android.synthetic.main.component_network_error_banner.*
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import timber.log.Timber
import javax.inject.Inject


class ContactsFragment : BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var networkViewModel: NetworkViewModel
    lateinit var contactsViewModel: ContactsViewModel
    lateinit var contactsAdapter: ContactsListAdapter
    lateinit var groupAdapter: GroupMembersListAdapter
    lateinit var navController: NavController
    lateinit var loadingProgressBar: LooperCircularProgressBar

    private var chooseModeCount = 0
    private var currSwipedAdapter: ContactsListAdapter? = null
    private var groupsListener = object : GroupsSelectionListener {
        override fun onAddMember(contact: ContactData) {
            if (chooseModeCount == 10) {
                context?.toast(R.string.contacts_max_members_msg)
                deselectContact(contact)
                return
            }
            groupAdapter.addMember(contact)
            contactsGroupRecyclerView.smoothScrollToPosition(groupAdapter.itemCount - 1)
            addMemberCount()
            verifyCreateGroup()
        }

        override fun onRemoveMember(viewHolder: ContactsViewHolder?, contact: ContactData, pos: Int) {
            groupAdapter.removeMember(contact)
            removeMemberCount()
            verifyCreateGroup()
        }

        private fun verifyCreateGroup() {
            toolbarGenericActionText?.isEnabled = groupAdapter.itemCount >= 2
        }

        override fun onRemoveMember(contact: ContactData, pos: Int) {
            deselectContact(contact)
            groupAdapter.removeMember(contact)
            removeMemberCount()
            verifyCreateGroup()
        }

        private fun addMemberCount() {
            chooseModeCount++
            toolbarGenericSubtitle.text = "($chooseModeCount/10)"
        }

        private fun removeMemberCount() {
            chooseModeCount--
            toolbarGenericSubtitle.text = "($chooseModeCount/10)"
        }
    }

    private fun deselectContact(contact: ContactData) {
        val contactPos = contactsAdapter.findPositionOf(contact)
        if (contactPos == -1) {
            return
        }

        val viewHolder =
            contactsRecyclerView.findViewHolderForAdapterPosition(contactPos) as ContactsViewHolder?
        viewHolder?.deselectContact()
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

        loadingProgressBar = LooperCircularProgressBar(requireContext(), false)
        contactsViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(ContactsViewModel::class.java)

        networkViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(NetworkViewModel::class.java)

        initComponents(view)
    }

    fun initComponents(root: View) {
        toolbarGeneric.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
        toolbarGenericTitle.text = getString(R.string.connections_title)

        toolbarGenericActionBtnLeft.apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_qr_scan
                )
            )
            visibility = View.VISIBLE
        }

        toolbarGenericActionBtn.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_menu_requests
            )
        )
        toolbarGenericActionBtn.visibility = View.VISIBLE

        startGroupsChat()
        startContactLists()

        contactsRequests.setOnSingleClickListener {
            findNavController().navigate(R.id.action_global_requests)
        }

        toolbarGenericBackBtn.setOnSingleClickListener {
            requireActivity().onBackPressed()
        }

        toolbarGenericActionBtnLeft.setOnSingleClickListener {
            findNavController().navigate(R.id.action_global_qr_code)
        }

        toolbarGenericActionBtn.setOnSingleClickListener {
            openAddContact()
        }

        contactsAddContactBtn.setOnSingleClickListener {
            openAddContact()
        }

        contactsSearchBar.doOnTextChanged { text, _, _, _ ->
            contactsAdapter.isSearching = !text.isNullOrEmpty()
            contactsAdapter.filter.filter(text)
        }

        contactsSearchBar.incognito(preferences.isIncognitoKeyboardEnabled)
    }

    private fun openAddContact() {
        navController.navigateSafe(R.id.action_contacts_fragment_to_ud_search_fragment)
    }

    private fun startGroupsChat() {
        groupAdapter = GroupMembersListAdapter(groupsListener)
        contactsGroupRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        contactsGroupRecyclerView.adapter = groupAdapter

        contactsNewGroup.setOnSingleClickListener {
            startContactSelection()
        }
    }

    private fun startContactSelection() {
        contactsRequests.visibility = View.GONE
        toolbarGenericActionBtnLeft.visibility = View.GONE
        chooseModeCount = 0
        contactsAdapter.chooseMode = true
        contactsAdapter.notifyItemRangeChanged(0, contactsAdapter.itemCount)

        toolbarGenericTitle.text = "Add members"
        toolbarGenericSubtitle.text = "(0/10)"
        toolbarGenericBackBtn.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_close_dark
            )
        )

        toolbarGenericBackBtn.setOnSingleClickListener {
            stopContactSelection()
        }

        toolbarGenericActionText.text = "Create"
        toolbarGenericSubtitle.visibility = View.VISIBLE
        toolbarGenericActionText.visibility = View.VISIBLE
        toolbarGenericActionBtn.visibility = View.GONE
        contactsNewGroup.visibility = View.GONE
        contactsRequests.visibility = View.GONE
        contactRequestsCount.visibility = View.GONE
        contactsGroupRecyclerView.visibility = View.VISIBLE
        toolbarGenericActionText.isEnabled = false
        toolbarGenericActionText.setOnSingleClickListener {
            showCreateGroupDialog()
        }
    }

    private fun createGroup() {
        PopupActionBottomDialog.getInstance(
            requireContext(),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_contacts_rounded),
            titleText = getString(R.string.group_create_dialog_title),
            subtitleText = getString(
                R.string.group_create_dialog_body,
                groupAdapter.itemCount+1
            ),
            positiveBtnText = getString(R.string.group_create_dialog_button),
            positiveBtnColor = R.color.brand_default,
            isInputDialog = true,
            isSecondInputDialog = true,
            isIncognito = preferences.isIncognitoKeyboardEnabled,
            textValidation = { input, _ ->
                return@getInstance input.length in 1..20
            },
            onClickPositive = { name, initialMsg ->
                contactsViewModel.createGroup(name, initialMsg, groupAdapter.getGroup())
            }, isCancellable = true
        ).show()
    }

    private fun showCreateGroupDialog() {
        val ui = CreateGroupDialogUI.create(
            getString(R.string.group_create_dialog_body, groupAdapter.itemCount+1),
            ::onCreateGroupClicked,
            ::showGroupInfoDialog
        )
        CreateGroupDialog.newInstance(ui)
            .show(requireActivity().supportFragmentManager, null)
    }

    private fun onCreateGroupClicked(name: String?, description: String?) {
        name?.let {
            contactsViewModel.createGroup(name, description, groupAdapter.getGroup())
        }
    }

    private fun showGroupInfoDialog() {
        showInfoDialog(
            R.string.group_create_dialog_info_title,
            R.string.group_create_dialog_info_body,
            null
        )
    }

    private fun stopContactSelection() {
        contactsRequests.visibility = View.VISIBLE
        toolbarGenericActionBtnLeft.visibility = View.VISIBLE
        groupAdapter.removeAll()
        contactsAdapter.deselectAll()
        contactsAdapter.chooseMode = false

        toolbarGenericTitle.text = getString(R.string.connections_title)
        toolbarGenericSubtitle.visibility = View.GONE
        toolbarGenericBackBtn.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_back_24dp
            )
        )

        toolbarGenericBackBtn.setOnSingleClickListener {
            requireActivity().onBackPressed()
        }

        toolbarGenericActionText.visibility = View.GONE
        toolbarGenericActionBtn.visibility = View.VISIBLE
        contactsNewGroup.visibility = View.VISIBLE
        contactsRequests.visibility = View.VISIBLE
        contactRequestsCount.visibility = View.VISIBLE
        contactsGroupRecyclerView.visibility = View.GONE
    }

    private fun startContactLists() {
        contactsAdapter = ContactsListAdapter(false, groupsListener)

        contactsRecyclerView.layoutManager = LinearLayoutManager(context)
        contactsRecyclerView.adapter = contactsAdapter
        contactsRecyclerView.itemAnimator = null

        maybeStartContactSelection()
    }

    private fun maybeStartContactSelection() {
        if (ContactsFragmentArgs.fromBundle(requireArguments()).contactSelectionMode) {
            startContactSelection()
        }
    }

    override fun onStart() {
        super.onStart()
        watchForEvents()
    }

    private fun watchForEvents() {
        contactsViewModel.requestsCount.observe(viewLifecycleOwner) { newCount ->
            if (newCount > 0) {
                contactRequestsCount.visibility = View.VISIBLE
                contactRequestsCount.text = newCount.toString()
            } else {
                contactRequestsCount.visibility = View.INVISIBLE
                contactRequestsCount.text = ""
            }
        }

        contactsViewModel.contactsData.observe(viewLifecycleOwner, {
            Timber.v("[CONTACTS LIST] Contacts updated")
        })

        contactsViewModel.groupsData.observe(viewLifecycleOwner, {
            Timber.v("[CONTACTS LIST] Groups updated")
        })

        contactsViewModel.combinedContactGroupsData.observe(viewLifecycleOwner, { combinedList ->
            val contactsList =
                combinedList.first.filter { item ->
                    item.status == RequestStatus.ACCEPTED.value
                } as List<Any>

            contactsAdapter.update(contactsList)
            checkListIsEmpty()
        })

//        networkViewModel.networkState.observe(
//            viewLifecycleOwner,
//            Observer { networkState ->
//                Timber.v("Network State: $networkState")
//                if (networkState == NetworkState.HAS_CONNECTION) {
//                    networkStatusLayout?.visibility = View.GONE
//                } else {
//                    val bannerMsg = networkViewModel.getNetworkStateMessage(networkState)
//                    networkStatusLayout?.visibility = View.VISIBLE
//                    networkStatusText?.text = bannerMsg
//                }
//            })

        contactsViewModel.newGroupRequestSent.observe(viewLifecycleOwner, { result ->
            when (result) {
                is DataRequestState.Start -> {
                    loadingProgressBar.show()
                }
                is DataRequestState.Error -> {
                    showError(result.error)
                    contactsViewModel.newGroupRequestSent.value = DataRequestState.Completed()
                }

                is DataRequestState.Success -> {
                    contactsViewModel.newGroupRequestSent.value = DataRequestState.Completed()
                    (requireActivity() as MainActivity).createSnackMessage(
                        "Group Chat requests were successfully sent!",
                        true
                    )
                    findNavController().navigateUp()
                }
                else -> {
                    loadingProgressBar.dismiss()
                }
            }
        })
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

    private fun showDeleteDialog(pos: Int) {
        PopupActionBottomDialogFragment.getInstance(
            titleText = "Are you sure you want to delete this contact?",
            subtitleText = "You will be removing this contact and all associated conversations.",
            positiveBtnText = "Delete Contact",
            negativeBtnText = "No, Don't Delete",
            onClickPositive = {
                currSwipedAdapter?.composedItemsList?.get(pos)?.let { contact ->

                }
            },
            isIncognito = preferences.isIncognitoKeyboardEnabled
        ).show(childFragmentManager, "contactsSwipeToDelete")
    }
}