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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import io.xxlabs.messenger.R
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.databinding.FragmentUserSearchBinding
import io.xxlabs.messenger.requests.ui.RequestsFragment
import io.xxlabs.messenger.requests.ui.nickname.SaveNicknameDialog
import io.xxlabs.messenger.requests.ui.send.OutgoingRequest
import io.xxlabs.messenger.requests.ui.send.SendRequestDialog
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.toast.CustomToastActivity
import io.xxlabs.messenger.support.toast.ToastUI
import io.xxlabs.messenger.ui.base.ViewPagerFragmentStateAdapter
import io.xxlabs.messenger.ui.dialog.info.InfoDialog
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialog
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialogUI
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.main.countrycode.CountryFullscreenDialog
import io.xxlabs.messenger.ui.main.countrycode.CountrySelectionListener
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
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

    override fun initToolbar() {
        binding.userSearchAppBarLayout.apply {
            toolbarGeneric.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
            toolbarGenericActionText.visibility = View.VISIBLE
            toolbarGenericTitle.text = requireContext().getString(R.string.search_title)
            toolbarGenericBackBtn.setOnClickListener { navController.navigateUp() }
        }
    }

    override fun initViewPager(root: View) {
        binding.apply {
            setupViewPager(userSearchViewPager)
            TabLayoutMediator(userSearchAppBarTabs, userSearchViewPager) { tab, position ->
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

            userSearchAppBarTabs.apply {
                addOnTabSelectedListener(
                    object : OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            if (searchViewModel.previousTabPosition != SEARCH_QR && tab?.position == SEARCH_QR) {
                                searchViewModel.previousTabPosition = SEARCH_QR
                                lifecycleScope.launch {
                                    delay(500)
                                    navigateToQrCode()
                                }
                            } else {
                                searchViewModel.previousTabPosition = tab?.position ?: 0
                            }
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab?) { }
                        override fun onTabReselected(tab: TabLayout.Tab?) { }
                    }
                )
            }
        }
    }

    private fun navigateToQrCode() {
        val qrCodeScreen = UserSearchFragmentDirections.actionGlobalQrCode()
        findNavController().navigate(qrCodeScreen)
    }

    override fun setupViewPager(viewPager: ViewPager2) {
        stateAdapter = ViewPagerFragmentStateAdapter(childFragmentManager, lifecycle)
        stateAdapter.addFragment(
            UsernameSearchFragment(),
            "Username",
            getTabIcon(R.drawable.ic_contact_light)
        )
        stateAdapter.addFragment(
            EmailSearchFragment(),
            "Email",
            getTabIcon(R.drawable.ic_mail)
        )
        stateAdapter.addFragment(
            PhoneSearchFragment(),
            "Phone",
            getTabIcon(R.drawable.ic_phone)
        )
        stateAdapter.addFragment(
            QrSearchFragment(),
            "QR Code",
            getTabIcon(R.drawable.ic_qr_code_label)
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

    override fun onResume() {
        super.onResume()
        resetTabPosition()
    }

    private fun resetTabPosition() {
        binding.userSearchAppBarTabs.setScrollPosition(SEARCH_USERNAME, 0f, true)
        binding.userSearchViewPager.currentItem = SEARCH_USERNAME
    }

    private fun observeUi() {
        searchViewModel.udSearchUi.observe(viewLifecycleOwner) { state ->
            binding.ui = state
        }

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

        searchViewModel.searchInfoDialog.observe(viewLifecycleOwner) { ui ->
            ui?.let {
                showDialog(ui)
                searchViewModel.onInfoDialogShown()
            }
        }

        searchViewModel.selectCountry.observe(viewLifecycleOwner) { listener ->
            listener?.let {
                selectCountry(it)
            }
        }

        searchViewModel.dismissCountries.observe(viewLifecycleOwner) { dismiss ->
            if (dismiss) {
                dismissCountryList()
                searchViewModel.onCountriesDismissed()
            }
        }

        requestsViewModel.showContactRequestDialog.observe(viewLifecycleOwner) { user ->
            user?.let {
                showSendRequestDialog(it)
                requestsViewModel.onSendRequestDialogShown()
            }
        }
    }

    private var countryList: CountryFullscreenDialog? = null

    private fun selectCountry(listener: CountrySelectionListener) {
        safelyInvoke {
            countryList = CountryFullscreenDialog.getInstance(listener)
            countryList?.show(parentFragmentManager, null)
        }
    }

    private fun dismissCountryList() {
        try {
            countryList?.dismiss()
            countryList = null
        } catch (e: Exception) {
            Timber.d("Exception occured when dismissing countries list: ${e.message}")
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

    private fun showDialog(ui: InfoDialogUI) {
        safelyInvoke {
            InfoDialog
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

    private fun showSendRequestDialog(user: ContactData) {
        safelyInvoke {
            SendRequestDialog
                .newInstance(user)
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