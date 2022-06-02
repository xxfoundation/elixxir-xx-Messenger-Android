package io.xxlabs.messenger.support.toolbar

import android.text.Spanned
import io.xxlabs.messenger.R

interface ToolbarListener {
    fun onBackPressed()
}

data class CustomToolbar(
    private val listener: ToolbarListener,
    override val title: Spanned,
    override val menuItems: List<ToolbarMenuItem>
) : ToolbarUI {
    override fun onBackPressed() = listener.onBackPressed()
}

interface MenuItemListener {
    fun onClick(item: ToolbarMenuItem)
}

data class ToolbarItem(
    private val listener: MenuItemListener,
    override val id: Int,
    override val icon: Int?,
    override val label: Int?,
    override val textColor: Int = R.color.brand_default,
    override val iconTint: Int = R.color.neutral_active,
    override val enabled: Boolean = true
) : ToolbarMenuItem {
    override fun onClick(item: ToolbarMenuItem) = listener.onClick(item)
}