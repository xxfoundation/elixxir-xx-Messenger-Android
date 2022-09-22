package io.xxlabs.messenger.requests.deprecated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.extensions.disableWithAlpha
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.support.view.LooperCircularProgressBar
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_requests_generic.*
import timber.log.Timber
import javax.inject.Inject

class RequestGenericFragment : BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var networkViewModel: NetworkViewModel
    lateinit var contactsViewModel: ContactsViewModel

    lateinit var progress: LooperCircularProgressBar
    private lateinit var navController: NavController
    private lateinit var resultsAdapter: RequestsAdapter

    val filter: RequestsFilter by lazy {
        arguments?.get("filter") as RequestsFilter? ?: RequestsFilter.RECEIVED
    }

    private var currentAddButton: View? = null
    private var currentContact: ContactData? = null

    private var selectionListener = object : RequestsListener {
        override fun onResend(v: View, contact: ContactData) {
            v.disableWithAlpha()
            currentAddButton = v
            when (RequestStatus.from(contact.status)) {
                RequestStatus.SEND_FAIL, RequestStatus.SENT -> {
                    Timber.v("Resending request auth channel...")
                    contactsViewModel.updateAndRequestAuthChannel(contact)
                    (requireActivity() as MainActivity).createSnackMessage(
                        "Sending a new request",
                        true
                    )
                }
                RequestStatus.CONFIRM_FAIL -> {
                    Timber.v("Resending confirm auth channel...")
                    contactsViewModel.confirmAuthenticatedChannel(contact)

                    (requireActivity() as MainActivity).createSnackMessage(
                        "Sending a new confirmation",
                        true
                    )
                }
                RequestStatus.RESET_SENT, RequestStatus.RESET_FAIL -> {
                    contactsViewModel.resetSession(contact)
                }
                else -> { }
            }
        }

        override fun onClickUsername(v: View, contact: ContactData) {
            val bundle = bundleOf("contact_id" to contact.userId)
            if (contact.status == RequestStatus.VERIFIED.value) {
                navController.navigateSafe(R.id.action_global_contact_invitation, bundle)
            } else {
                navController.navigateSafe(R.id.action_global_contact_details, bundle)
            }
        }

        override fun onClickAcceptContact(pos: Int, contact: ContactData) {
            currentContact = contact
            acceptContact(contact)
        }

        override fun onClickRejectContact(pos: Int, contact: ContactData) {
            rejectContact(contact)
        }

        override fun onClickAcceptGroup(pos: Int, group: GroupData) {
            acceptGroup(group)
        }

        override fun onClickRejectGroup(pos: Int, group: GroupData) {
            rejectGroup(group, pos)
        }

        override fun onRetry(contact: ContactData) {
            contactsViewModel.verifyNewRequest(contact)
        }

        override fun onVerifying(contact: ContactData) {
            displayVerifyingPopup()
            onRetry(contact)
        }
    }

    private fun displayVerifyingPopup(){
        PopupActionBottomDialogFragment.getInstance(
            titleText = getString(R.string.request_verifying_popup_title),
            subtitleText = getString(R.string.request_verifying_popup_message),
            positiveBtnText = getString(android.R.string.ok),
            onClickPositive = { },
            isIncognito = preferences.isIncognitoKeyboardEnabled
        ).show(childFragmentManager, "requestScreenVerifyingRequestDialog")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[NetworkViewModel::class.java]

        contactsViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[ContactsViewModel::class.java]

        navController = findNavController()
        progress = LooperCircularProgressBar(requireContext(), false)
        initComponents(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_requests_generic, container, false)
    }

    fun initComponents(root: View) {
        requestsAddContactBtn.setOnSingleClickListener {
            navController.navigateSafe(R.id.action_requests_to_ud_search)
        }
        addResultsRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        changeFilter()
        watchForChanges()
    }

    private fun addResultsRecyclerView() {
        resultsAdapter = RequestsAdapter(selectionListener)
        val layoutManager = LinearLayoutManager(context)

        requestsRecyclerView.layoutManager = layoutManager
        requestsRecyclerView.adapter = resultsAdapter
        requestsRecyclerView.itemAnimator = null
    }

    private fun changeFilter() {
        when (filter) {
            RequestsFilter.RECEIVED -> filterByReceived()
            RequestsFilter.SENT -> filterBySent()
            RequestsFilter.FAILED -> filterByFailed()
        }
    }

    private fun hideEmptyMsg() {
        requestsEmptyTitle.visibility = View.GONE
        requestsAddContactBtn.visibility = View.GONE
    }

    private fun showEmptyMsg(filter: RequestsFilter) {
        requestsEmptyTitle.visibility = View.VISIBLE

        val emptyTitle = "You have no ${filter.name.lowercase()} requests"
        requestsEmptyTitle.text = emptyTitle
        if (filter == RequestsFilter.SENT) {
            requestsEmptyTitle.visibility = View.GONE
            requestsAddContactBtn.visibility = View.VISIBLE
        } else {
            requestsEmptyTitle.visibility = View.VISIBLE
            requestsAddContactBtn.visibility = View.GONE
        }
    }

    private fun watchForChanges() {
        contactsViewModel.newConfirmRequestSent.observe(viewLifecycleOwner) { result ->
            Timber.v("Trying to send  new confirm request")

            when (result) {
                is DataRequestState.Start -> {
                    progress.show()
                }
                is DataRequestState.Error -> {
                    showError(result.error, isBindingError = true)
                    completeInvitation()
                }

                is DataRequestState.Success -> {
                    completeInvitation()
                    (requireActivity() as MainActivity).createSnackMessage("Request Confirmed!")
                }
                else -> {
                    progress.hide()
                }
            }
        }

        contactsViewModel.acceptGroupRequest.observe(viewLifecycleOwner, { result ->
            Timber.v("[GROUP CHAT] Accepting group chat invitation...")
            when (result) {
                is DataRequestState.Start -> {
                    progress.show()
                }
                is DataRequestState.Error -> {
                    showError(result.error, isBindingError = true)
                    completeInvitation(true)
                }

                is DataRequestState.Success -> {
                    completeInvitation(true)
                    (requireActivity() as MainActivity).createSnackMessage("Group Confirmed!")
                }
                else -> {
                    progress.hide()
                }
            }
        })
    }

    private fun acceptContact(contact: ContactData) {
        createName(contact)
    }

    private fun createName(contact: ContactData) {
        PopupActionBottomDialogFragment.getInstance(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_contact_rounded),
            titleText = "Create a Contact",
            positiveBtnText = "Save Contact",
            positiveBtnColor = R.color.brand_dark,
            isInputDialog = true,
            defaultInput = contact.username,
            isIncognito = preferences.isIncognitoKeyboardEnabled,
            textValidation = {
                return@getInstance it.length > 3
            },
            onClickPositive = { name ->
                contact.nickname = name
                Timber.v("Confirming contact request with ${contact.userId}")
                Timber.d("Contact data:")
                Timber.d("Contact name ${contact.nickname}")
                Timber.d("Contact username ${contact.username}")
                Timber.d("Contact email ${contact.email}")
                Timber.d("Contact phone ${contact.phone}")
                Timber.d("Contact bytes ${contact.marshaled}")
                contactsViewModel.confirmAuthenticatedChannel(contact)
            },
            isCancellable = true,
            onDismissedByUser = ::onAcceptDialogDismissed
        ).show(childFragmentManager, "createNameDialog")
    }

    private fun onAcceptDialogDismissed(){
        resultsAdapter.notifyDataSetChanged()
    }

    private fun rejectContact(contact: ContactData) {
        contactsViewModel.rejectContact(contact)
    }

    private fun acceptGroup(group: GroupData) {
        contactsViewModel.acceptGroup(group)
    }

    private fun rejectGroup(group: GroupData, position: Int) {
        contactsViewModel.rejectGroup(group, position)
    }

    private fun completeInvitation(isGroup: Boolean = false) {
        if (isGroup) {
            contactsViewModel.acceptGroupRequest.postValue(DataRequestState.Completed())
        } else {
            contactsViewModel.newConfirmRequestSent.postValue(DataRequestState.Completed())
        }
    }

    private fun filterByFailed() {
        contactsViewModel.contactsData.observe(viewLifecycleOwner) { contacts ->
            val requestsList = contacts.filter {
                it.status == RequestStatus.SEND_FAIL.value
                        || it.status == RequestStatus.CONFIRM_FAIL.value
                        || it.status == RequestStatus.RESET_FAIL.value
            }

            if (requestsList.isEmpty()) showEmptyMsg(filter)
            else hideEmptyMsg()

            resultsAdapter.submitList(requestsList)
        }
    }

    private fun filterBySent() {
        contactsViewModel.contactsData.observe(viewLifecycleOwner) { contacts ->
            val requestsList = contacts.filter {
                it.status == RequestStatus.SENT.value
                        || it.status == RequestStatus.RESET_SENT.value
            }

            if (requestsList.isEmpty()) showEmptyMsg(filter)
            else hideEmptyMsg()

            resultsAdapter.submitList(requestsList)
        }
    }

    private fun filterByReceived() {
        val receivedRequests = MediatorLiveData<List<Any>>().apply {
            var contactRequests = listOf<Any>()
            var groupInvites = listOf<Any>()
            var receivedList: List<Any>

            addSource(contactsViewModel.contactsData) { contacts ->
                contactRequests = contacts.filter {
                    it.status == RequestStatus.VERIFIED.value
                        || it.status == RequestStatus.VERIFYING.value
                        || it.status == RequestStatus.VERIFICATION_FAIL.value
                }
                receivedList = contactRequests + groupInvites
                value = receivedList
            }

            addSource(contactsViewModel.groupsData) { groups ->
                groupInvites = groups.filter {
                    it.status != RequestStatus.ACCEPTED.value
                }
                receivedList = contactRequests + groupInvites
                value = receivedList
            }
        }

        receivedRequests.observe(viewLifecycleOwner) { requestsList ->
            if (requestsList.isEmpty()) showEmptyMsg(filter)
            else hideEmptyMsg()

            resultsAdapter.submitList(requestsList)
        }
    }

    companion object {
        fun newInstance(filter: RequestsFilter): RequestGenericFragment {
            val bundle = bundleOf("filter" to filter)
            val fragment = RequestGenericFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}