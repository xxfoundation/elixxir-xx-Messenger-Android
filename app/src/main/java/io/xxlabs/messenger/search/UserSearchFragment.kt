package io.xxlabs.messenger.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import io.xxlabs.messenger.support.toast.ToastUI
import io.xxlabs.messenger.ui.base.ViewPagerFragmentStateAdapter
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialog
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialogUI
import io.xxlabs.messenger.ui.global.ContactsViewModel
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_requests.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Exception

class UserSearchFragment : RequestsFragment() {

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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeUI()
            }
        }
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

    override fun onStart() {
        super.onStart()
        observeUi()
    }

    private fun observeUi() {
        super.observeUI()

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

        searchViewModel.dialogUi.observe(viewLifecycleOwner) { ui ->
            ui?.let {
                showDialog(ui)
                searchViewModel.onDialogShown()
            }
        }

        searchViewModel.toastUi.observe(viewLifecycleOwner) { ui ->
            ui?.let {
                showToast(ui)
                searchViewModel.onToastShown()
            }
        }
    }

    private fun showToast(ui: ToastUI) {
        safelyInvoke {
            toastHandler.showCustomToast(ui)
        }
    }

    private fun showDialog(ui: TwoButtonInfoDialogUI) {
        safelyInvoke {
            TwoButtonInfoDialog
                .newInstance(ui)
                .show(parentFragmentManager, null)
        }
    }

    private fun showSaveNicknameDialog(outgoingRequest: OutgoingRequest) {
        safelyInvoke {
            SaveNicknameDialog
                .newInstance(outgoingRequest)
                .show(childFragmentManager, null)
        }
    }

    private fun showSendRequestDialog(user: ContactWrapperBase) {
        safelyInvoke {
            SendRequestDialog
                .newInstance(ContactData.from(user))
                .show(childFragmentManager, null)
        }
    }

    companion object {
        const val SEARCH_USERNAME = 0
        const val SEARCH_EMAIL = 1
        const val SEARCH_PHONE = 2
        const val SEARCH_QR = 3
    }
}