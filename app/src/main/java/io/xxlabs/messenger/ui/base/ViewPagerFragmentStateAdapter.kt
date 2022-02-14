package io.xxlabs.messenger.ui.base

import android.graphics.drawable.Drawable
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerFragmentStateAdapter(
    @NonNull fragmentManager: FragmentManager,
    @NonNull lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val fragmentsList = mutableListOf<Fragment>()
    private val fragmentTitlesList = mutableListOf<String>()
    private val fragmentIconsList = mutableMapOf<Int, Drawable?>()

    fun addFragment(fragment: Fragment, title: String, drawable: Drawable? = null) {
        if (!fragmentsList.contains(fragment)) {
            fragmentsList.add(fragment)
            fragmentTitlesList.add(title)
            fragmentIconsList[fragmentsList.indexOf(fragment)] = drawable
        }
    }

    fun getFragment(position: Int): Fragment {
        return fragmentsList[position]
    }

    fun getPageTitle(position: Int): CharSequence {
        return fragmentTitlesList[position]
    }

    @NonNull
    override fun createFragment(position: Int): Fragment {
        return fragmentsList[position]
    }

    override fun getItemCount(): Int {
        return fragmentsList.size
    }

    fun getIcon(position: Int): Drawable? {
        return fragmentIconsList[position]
    }
}