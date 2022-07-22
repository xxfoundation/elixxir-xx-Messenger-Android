package io.xxlabs.messenger.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.xxlabs.messenger.R
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.databinding.FragmentUserSearchBinding
import io.xxlabs.messenger.requests.ui.RequestsFragment
import io.xxlabs.messenger.requests.ui.list.FailedRequestsFragment
import io.xxlabs.messenger.requests.ui.list.ReceivedRequestsFragment
import io.xxlabs.messenger.requests.ui.list.SentRequestsFragment
import io.xxlabs.messenger.requests.ui.nickname.SaveNicknameDialog
import io.xxlabs.messenger.requests.ui.send.OutgoingRequest
import io.xxlabs.messenger.requests.ui.send.SendRequestDialog
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.toast.CustomToastActivity
import io.xxlabs.messenger.ui.base.ViewPagerFragmentStateAdapter
import io.xxlabs.messenger.ui.dialog.info.showTwoButtonInfoDialog
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.main.MainViewModel
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_requests.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.Exception

class UserSearchFragment : RequestsFragment() {

    private val mainViewModel: MainViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { viewModelFactory }
    )
    private val contactsViewModel: ContactsViewModel by viewModels { viewModelFactory }
    private val searchViewModel: UserSearchViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentUserSearchBinding
    override val navController: NavController by lazy {
        findNavController()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as? CustomToastActivity)?.run {
            toastHandler = this
        } ?: throw Exception("Activity must implement CustomToastActivity!")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserSearchBinding.inflate(
            inflater, container, false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initViewPager(view)
        showNewUserPopups()
    }

    override fun initToolbar() {
        binding.userSearchAppBarLayout.apply {
            toolbarGeneric.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
            toolbarGenericActionText.visibility = View.VISIBLE
            toolbarGenericTitle.text = requireContext().getString(R.string.search_title)
            toolbarGenericBackBtn.setOnClickListener { navController.navigateUp() }
        }
    }

    override fun initViewPager(root: View) {
        binding.userSearchViewPager.apply {
            setupViewPager(this)
            TabLayoutMediator(requestsAppBarTabs, requestsViewPager) { tab, position ->
                tab.apply {
                    text = stateAdapter.getPageTitle(position)
                    icon = stateAdapter.getIcon(position)
                    contentDescription = when (position) {
                        SEARCH_USERNAME -> "search.tab.username"
                        SEARCH_EMAIL -> "search.tab.email"
                        SEARCH_PHONE -> "search.tab.phone"
                        SEARCH_QR -> "search.tab.qr"
                        else -> "search.tab.invalid"
                    }
                }
            }.attach()
        }
    }

    override fun setupViewPager(viewPager: ViewPager2) {
        stateAdapter = ViewPagerFragmentStateAdapter(childFragmentManager, lifecycle)
        stateAdapter.addFragment(
            ReceivedRequestsFragment(),
            "Username",
            getTabIcon(R.drawable.ic_mail_received)
        )
        stateAdapter.addFragment(
            SentRequestsFragment(),
            "Email",
            getTabIcon(R.drawable.ic_mail_sent)
        )
        stateAdapter.addFragment(
            FailedRequestsFragment(),
            "Phone",
            getTabIcon(R.drawable.ic_danger)
        )
        stateAdapter.addFragment(
            FailedRequestsFragment(),
            "QR Code",
            getTabIcon(R.drawable.ic_danger)
        )

        viewPager.adapter = stateAdapter
        viewPager.offscreenPageLimit = 3

        val selectedTab = arguments?.getInt("selectedTab") ?: 0
        viewPager.setCurrentItem(selectedTab, false)
    }

    private fun showNewUserPopups() {
        showNotificationDialog()
    }

    // TODO: This event should be launched by observing ViewModel.
    private fun showNotificationDialog() {
        if (preferences.userData.isNotBlank() && preferences.isFirstTimeNotifications) {
            showTwoButtonInfoDialog(
                title = R.string.settings_push_notifications_dialog_title,
                body = R.string.settings_push_notifications_dialog_body,
                linkTextToUrlMap = null,
                positiveClick = ::enablePushNotifications,
                negativeClick = null,
                onDismiss = ::showCoverMessageDialog
            )
            preferences.isFirstTimeNotifications = false
        }
    }

    // TODO: This event should be launched by observing ViewModel.
    private fun enablePushNotifications() {
        mainViewModel.enableNotifications { error ->
            error?.let { showError(error) }
        }
    }

    // TODO: This event should be launched by observing ViewModel.
    private fun showCoverMessageDialog() {
        if (preferences.userData.isNotBlank() && preferences.isFirstTimeCoverMessages) {
            showTwoButtonInfoDialog(
                R.string.settings_cover_traffic_title,
                R.string.settings_cover_traffic_dialog_body,
                mapOf(
                    getString(R.string.settings_cover_traffic_link_text)
                            to getString(R.string.settings_cover_traffic_link_url)
                ),
                ::enableCoverMessages,
                ::declineCoverMessages,
            )
            preferences.isFirstTimeCoverMessages = false
        }
    }

    // TODO: Redundant, remove.
    private fun enableCoverMessages() {
        enableDummyTraffic(true)
    }

    // TODO: Redundant, remove.
    private fun declineCoverMessages() {
        enableDummyTraffic(false)
    }

    // TODO: This event should be launched by observing ViewModel.
    private fun enableDummyTraffic(enabled: Boolean) {
        try {
            mainViewModel.enableDummyTraffic(enabled)
        } catch (e: Exception) {
            showError(e, true)
        }
    }

    override fun onStart() {
        super.onStart()
        observeUi()
    }

    private fun observeUi() {
        requestsViewModel.sendContactRequest.onEach { toUser ->
            toUser?.let {
                contactsViewModel.updateAndRequestAuthChannel(toUser)
                requestsViewModel.onSendRequestHandled()
            }
        }.launchIn(lifecycleScope)

        requestsViewModel.showCreateNickname.onEach { outgoingRequest ->
            outgoingRequest?.let {
                showSaveNicknameDialog(it)
                requestsViewModel.onShowCreateNicknameHandled()
            }
        }.launchIn(lifecycleScope)
    }

    private fun showSaveNicknameDialog(outgoingRequest: OutgoingRequest) {
        SaveNicknameDialog
            .newInstance(outgoingRequest)
            .show(childFragmentManager, null)
    }

    private fun showSendRequestDialog(user: ContactWrapperBase) {
        SendRequestDialog
            .newInstance(ContactData.from(user))
            .show(childFragmentManager, null)
    }

    companion object {
        const val SEARCH_USERNAME = 0
        const val SEARCH_EMAIL = 1
        const val SEARCH_PHONE = 2
        const val SEARCH_QR = 3
    }
}