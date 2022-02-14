package io.xxlabs.messenger.ui.main.qrcode

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.support.extensions.changeStatusBarIconTheme
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.base.ViewPagerFragmentStateAdapter
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
        watchForChanges()
    }

    fun initComponents(root: View) {
        root.setInsets(
            bottomMask = 0,
        )

        root.apply {
            setupViewPager(qrCodeViewPager)
            TabLayoutMediator(qrCodeTabs, qrCodeViewPager) { tab, position ->
                tab.text = stateAdapter.getPageTitle(position)
                tab.icon = stateAdapter.getIcon(position)
                tab.contentDescription = if (position == 0) {
                    "qrcode.tab.scan"
                } else {
                    "qrcode.tab.mycode"
                }
            }.attach()

            qrCodeBackBtn.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        stateAdapter = ViewPagerFragmentStateAdapter(childFragmentManager, lifecycle)
        stateAdapter.addFragment(QrCodeScanFragment(), "Scan Code")
        stateAdapter.addFragment(QrCodeShowFragment(), "My Code", getLabelIcon())

        viewPager.adapter = stateAdapter
        viewPager.setPageTransformer(MarginPageTransformer(1500))
        viewPager.setPageTransformer(mAnimator)
        viewPager.offscreenPageLimit = 2
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    qrCodeViewModel.disableScanner = false
                    setupScanTabColors()
                    qrCodeViewModel.setWindowBackgroundColor(null)
                } else {
                    qrCodeViewModel.disableScanner = true
                    setupMyCodeColors()
                }
            }
        })
    }

    private fun setupScanTabColors() {
        qrCodeTabs.tabTextColors =
            ContextCompat.getColorStateList(requireContext(), R.color.selector_qr_code_tab_primary)
       qrCodeBackBtn.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(requireContext(), R.color.white),
            PorterDuff.Mode.SRC_IN
        )
        qrCodeTabs.tabIconTint =
            ContextCompat.getColorStateList(requireContext(), R.color.selector_qr_code_tab_primary)

        requireActivity().changeStatusBarIconTheme(lightMode = false)
    }

    private fun getLabelIcon(): Drawable? {
        return ContextCompat.getDrawable(requireContext(), R.drawable.ic_qr_code_label)
    }

    private fun setupMyCodeColors() {
        qrCodeTabs.tabTextColors = ContextCompat.getColorStateList(
            requireContext(),
            R.color.selector_qr_code_tab_secondary
        )
        qrCodeViewModel.setWindowBackgroundColor(R.color.neutral_off_white)
        qrCodeBackBtn.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(requireContext(), R.color.neutral_active),
            PorterDuff.Mode.SRC_IN
        )
        qrCodeTabs.tabIconTint = ContextCompat.getColorStateList(
            requireContext(),
            R.color.selector_qr_code_tab_secondary
        )

        requireActivity().changeStatusBarIconTheme(lightMode = true)
    }

    private fun watchForChanges() {
        qrCodeViewModel.background.observe(viewLifecycleOwner, Observer { newColor ->
            newColor?.let {
                setCameraBgColor(newColor)
            }
        })

        qrCodeViewModel.scanNavigation.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is DataRequestState.Success -> {
                    findNavController().navigateSafe(
                        R.id.action_qr_code_to_qr_code_success,
                        result.data
                    )
                    qrCodeViewModel.scanNavigation.postValue(DataRequestState.Completed())
                }

                is DataRequestState.Error -> {
                    findNavController().navigateUp()
                    qrCodeViewModel.scanNavigation.postValue(DataRequestState.Completed())
                }

                else -> {
                }
            }
        })
    }

    private fun setCameraBgColor(color: Int) {
        requireActivity().findViewById<ViewGroup>(R.id.qrCodeRoot)
            ?.setBackgroundColor(ContextCompat.getColor(requireContext(), color))
    }

    private val mAnimator = ViewPager2.PageTransformer { page, _ ->
        page.apply {
            scaleX = 1f
            scaleY = 1f
        }
    }
}