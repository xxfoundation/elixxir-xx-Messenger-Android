package io.xxlabs.messenger.requests.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.requests.model.ContactRequest
import io.xxlabs.messenger.requests.model.GroupInvitation
import io.xxlabs.messenger.requests.ui.accepted.contact.RequestAcceptedDialog
import io.xxlabs.messenger.requests.ui.accepted.group.InvitationAcceptedDialog
import io.xxlabs.messenger.requests.ui.details.contact.RequestDetailsDialog
import io.xxlabs.messenger.requests.ui.details.group.InvitationDetailsDialog
import io.xxlabs.messenger.requests.ui.list.FailedRequestsFragment
import io.xxlabs.messenger.requests.ui.list.ReceivedRequestsFragment
import io.xxlabs.messenger.requests.ui.list.SentRequestsFragment
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.toast.CustomToastActivity
import io.xxlabs.messenger.support.toast.ToastUI
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.base.ViewPagerFragmentStateAdapter
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_requests.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class RequestsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val requestsViewModel: RequestsViewModel by viewModels { viewModelFactory }

    private lateinit var navController: NavController
    private lateinit var stateAdapter: ViewPagerFragmentStateAdapter

    private lateinit var toastHandler : CustomToastActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as? CustomToastActivity)?.run {
            toastHandler = this
        } ?: throw Exception("Activity must implement CustomToastActivity!")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeUI()
            }
        }
        return inflater.inflate(R.layout.fragment_requests, container, false)
    }

    private fun observeUI() {
        requestsViewModel.showReceivedRequestDetails.onEach { request ->
            request?.let {
                showRequestDetails(request)
                requestsViewModel.onRequestDialogShown()
            }
        }.launchIn(lifecycleScope)

        requestsViewModel.showConnectionAccepted.onEach { request ->
            request?.let {
                showAccepted(request)
                requestsViewModel.onNewConnectionShown()
            }
        }.launchIn(lifecycleScope)

        requestsViewModel.navigateToMessages.onEach { contact ->
            contact?.let {
                navigateToChat(contact)
                requestsViewModel.onNavigateToMessagesHandled()
            }
        }.launchIn(lifecycleScope)

        requestsViewModel.showInvitationDetails.onEach { invitation ->
            invitation?.let {
                showInvitationDetails(invitation)
                requestsViewModel.onInvitationDialogShown()
            }
        }.launchIn(lifecycleScope)

        requestsViewModel.showGroupAccepted.onEach { group ->
            group?.let {
                showJoined(group)
                requestsViewModel.onGroupAcceptedShown()
            }
        }.launchIn(lifecycleScope)


        requestsViewModel.navigateToGroupChat.onEach { group ->
            group?.let {
                navigateToGroup(group)
                requestsViewModel.onNavigateToGroupHandled()
            }
        }.launchIn(lifecycleScope)

        requestsViewModel.customToast.onEach { ui ->
            ui?.let {
                showCustomToast(ui)
                requestsViewModel.onShowToastHandled()
            }
        }.launchIn(lifecycleScope)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        initComponents(view)
    }

    fun initComponents(root: View) {
        toolbarGeneric.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
        toolbarGenericTitle.text = "Requests"
        bindListeners()

        root.apply {
            setupViewPager(requestsViewPager)
            TabLayoutMediator(requestsAppBarTabs, requestsViewPager) { tab, position ->
                tab.apply {
                    text = stateAdapter.getPageTitle(position)
                    icon = stateAdapter.getIcon(position)
                    contentDescription = when (position) {
                        REQUESTS_TAB_RECEIVED -> "requests.tab.received"
                        REQUESTS_TAB_SENT -> "requests.tab.sent"
                        else -> "requests.tab.failed"
                    }
                }
            }.attach()
        }
    }

    private fun bindListeners() {
        toolbarGenericBackBtn.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        stateAdapter = ViewPagerFragmentStateAdapter(childFragmentManager, lifecycle)
        stateAdapter.addFragment(
            ReceivedRequestsFragment(),
            "Received",
            getTabIcon(R.drawable.ic_mail_received)
        )
        stateAdapter.addFragment(
            SentRequestsFragment(),
            "Sent",
            getTabIcon(R.drawable.ic_mail_sent)
        )
        stateAdapter.addFragment(
            FailedRequestsFragment(),
            "Failed",
            getTabIcon(R.drawable.ic_danger)
        )

        viewPager.adapter = stateAdapter
        viewPager.offscreenPageLimit = 3

        val selectedTab = arguments?.getInt("selectedTab") ?: 0
        viewPager.setCurrentItem(selectedTab, false)
    }

    private fun getTabIcon(resourceId: Int): Drawable? {
        return try {
            ResourcesCompat.getDrawable(resources, resourceId, null)
        } catch (e: Exception) {
            null
        }
    }

    private fun showRequestDetails(request: ContactRequest) {
        RequestDetailsDialog
            .newInstance(request)
            .show(childFragmentManager, null)
    }

    private fun showInvitationDetails(invitation: GroupInvitation) {
        InvitationDetailsDialog
            .newInstance(invitation)
            .show(childFragmentManager, null)
    }

    private fun showAccepted(contact: Contact) {
        RequestAcceptedDialog
            .newInstance(contact)
            .show(childFragmentManager, null)
    }

    private fun navigateToChat(contact: Contact) {
        val privateMessages = RequestsFragmentDirections.actionGlobalChat()
        privateMessages.contactId = contact.userId.toBase64String()
        privateMessages.contact = (contact as ContactData).copy(status = RequestStatus.ACCEPTED.value)
        findNavController().navigate(privateMessages)
    }

    private fun showJoined(group: Group) {
        InvitationAcceptedDialog
            .newInstance(group)
            .show(childFragmentManager, null)
    }

    private fun showCustomToast(ui: ToastUI) {
        toastHandler.showCustomToast(ui)
    }

    private fun navigateToGroup(group: Group) {
        val groupMessages = RequestsFragmentDirections.actionGlobalGroupsChat()
        groupMessages.groupId = group.groupId.toBase64String()
        groupMessages.group = (group as GroupData).copy(status = RequestStatus.ACCEPTED.value)
        findNavController().navigate(groupMessages)
    }

    companion object {
        const val REQUESTS_TAB_RECEIVED = 0
        const val REQUESTS_TAB_SENT = 1
        const val REQUESTS_TAB_FAILED = 2
    }
}