package io.xxlabs.messenger.requests.deprecated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.base.ViewPagerFragmentStateAdapter
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_requests.*


open class RequestsFragment : BaseFragment() {
    private lateinit var navController: NavController
    private lateinit var stateAdapter: ViewPagerFragmentStateAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_requests, container, false)
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
                tab.text = stateAdapter.getPageTitle(position)
                tab.icon = stateAdapter.getIcon(position)
                tab.contentDescription = when (position) {
                    0 -> {
                        "requests.tab.received"
                    }
                    1 -> {
                        "requests.tab.sent"
                    }
                    else -> {
                        "requests.tab.failed"
                    }
                }
            }.attach()
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        stateAdapter = ViewPagerFragmentStateAdapter(childFragmentManager, lifecycle)
        stateAdapter.addFragment(
            RequestGenericFragment.newInstance(RequestsFilter.RECEIVED),
            "Received"
        )
        stateAdapter.addFragment(
            RequestGenericFragment.newInstance(RequestsFilter.SENT),
            "Sent"
        )
        stateAdapter.addFragment(
            RequestGenericFragment.newInstance(RequestsFilter.FAILED),
            "Failed"
        )

        viewPager.adapter = stateAdapter
        viewPager.setPageTransformer(MarginPageTransformer(1500))
        viewPager.setPageTransformer(mAnimator)
        viewPager.offscreenPageLimit = 3
        viewPager.isUserInputEnabled = false

        val selectedTab = arguments?.getInt("selectedTab") ?: 0
        viewPager.setCurrentItem(selectedTab, false)
    }

    private val mAnimator = ViewPager2.PageTransformer { page, _ ->
//        page.apply {
//            scaleX = 1f
//            scaleY = 1f
//        }
    }

    private fun bindListeners() {
        toolbarGenericBackBtn.setOnClickListener {
            navController.navigateUp()
        }
    }
}
