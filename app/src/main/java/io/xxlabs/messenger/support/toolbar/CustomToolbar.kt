package io.xxlabs.messenger.support.toolbar

import android.text.Spanned
import io.xxlabs.messenger.R

interface ToolbarListener {
    fun onActionClicked()
}

data class CustomToolbar(
    private val listener: ToolbarListener,
    override val title: Spanned,
    override val menuItems: List<ToolbarMenuItem>
) : ToolbarUI {
    override fun onActionClicked() = listener.onActionClicked()
}

interface MenuItemListener {
    fun onClick(item: ToolbarMenuItem)
}

data class ToolbarItem(
    private val listener: MenuItemListener,
    override val id: Int,
    override val icon: Int? = null,
    override val label: Int? = null,
    override val textColor: Int = R.color.brand_default,
    override val iconTint: Int = R.color.neutral_active,
    override val enabled: Boolean = true
) : ToolbarMenuItem {
    override fun onClick(item: ToolbarMenuItem) = listener.onClick(item)
}