package io.xxlabs.messenger.ui.main.qrcode

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.base.ViewPagerFragmentStateAdapter
import io.xxlabs.messenger.ui.main.qrcode.scan.QrCodeScanFragment
import io.xxlabs.messenger.ui.main.qrcode.show.QrCodeShowFragment
import kotlinx.android.synthetic.main.fragment_qr_code.*
import javax.inject.Inject

class QrCodeFragment : BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var qrCodeViewModel: QrCodeViewModel

    private lateinit var stateAdapter: ViewPagerFragmentStateAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        qrCodeViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(QrCodeViewModel::class.java)

        return inflater.inflate(R.layout.fragment_qr_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponents(view)
    }

    fun initComponents(root: View) {
        root.apply {
            setInsets(bottomMask = 0)
            setupViewPager(qrCodeViewPager)
            TabLayoutMediator(qrCodeTabs, qrCodeViewPager) { tab, position ->
                tab.apply {
                    text = stateAdapter.getPageTitle(position)
                    icon = stateAdapter.getIcon(position)
                    contentDescription =
                        if (position == 0) "qrcode.tab.scan"
                        else "qrcode.tab.mycode"
                }
            }.attach()

            qrCodeBackBtn.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        stateAdapter = ViewPagerFragmentStateAdapter(childFragmentManager, lifecycle)
        stateAdapter.addFragment(QrCodeScanFragment(), "Scan Code", getScanIcon())
        stateAdapter.addFragment(QrCodeShowFragment(), "My Code", getMyCodeIcon())

        viewPager.adapter = stateAdapter
        viewPager.offscreenPageLimit = 2
    }

    private fun getScanIcon(): Drawable? =
        ContextCompat.getDrawable(requireContext(), R.drawable.ic_qr_scan)

    private fun getMyCodeIcon(): Drawable? =
        ContextCompat.getDrawable(requireContext(), R.drawable.ic_qr_code_label)

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        qrCodeViewModel.validUserData.observe(viewLifecycleOwner) { userData ->
            userData?.let {
                navigateToRequestSuccessful(userData)
                qrCodeViewModel.onUserDataHandled()
            }
        }

        qrCodeViewModel.navigateToChat.observe(viewLifecycleOwner) { contact ->
            contact?.let {
                navigateToChat(it)
                qrCodeViewModel.onNavigateToChatHandled()
            }
        }

        qrCodeViewModel.navigateToRequests.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                navigateToRequests()
                qrCodeViewModel.onNavigateToRequestsHandled()
            }
        }

        qrCodeViewModel.launchSettings.observe(viewLifecycleOwner) { clicked ->
            if (clicked) {
                openSettings()
                qrCodeViewModel.onLaunchSettingsHandled()
            }
        }
    }

    private fun navigateToRequestSuccessful(userData: String) {
        findNavController().navigateSafe(
            R.id.action_qr_code_to_qr_code_success,
            Bundle().apply {
                putString("contact", userData)
            }
        )
    }

    private fun navigateToChat(user: ContactData) {
        val chatDirections = QrCodeFragmentDirections.actionGlobalChat().apply {
            contact = user
            contactId = user.userId.toBase64String()
        }
        findNavController().navigateSafe(chatDirections.actionId)
    }

    private fun navigateToRequests() {
        val requestDirections = QrCodeFragmentDirections.actionGlobalRequests()
        findNavController().navigateSafe(requestDirections.actionId)
    }
}