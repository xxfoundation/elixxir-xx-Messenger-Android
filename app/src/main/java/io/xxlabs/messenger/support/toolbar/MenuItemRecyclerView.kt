package io.xxlabs.messenger.support.toolbar

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A non-scrollable RecyclerView that shows [ToolbarMenuItem]s horizontally.
 */
class MenuItemRecyclerView : RecyclerView {

    private val menuItemAdapter: MenuItemAdapter = MenuItemAdapter()
    private val menuItemLayoutManager =
        object : LinearLayoutManager(context, HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean = false
            override fun canScrollVertically(): Boolean = false
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(
        context: Context, attrs: AttributeSet, defStyle: Int
    ) : super(context, attrs, defStyle)

    init {
        adapter = menuItemAdapter
        layoutManager = menuItemLayoutManager
    }

    fun initMenu(menuItems: List<ToolbarMenuItem>) {
        menuItemAdapter.submitList(menuItems)
    }
}

@BindingAdapter("menuItems")
fun MenuItemRecyclerView.setMenuItems(menuItems: List<ToolbarMenuItem>) {
    initMenu(menuItems)
}