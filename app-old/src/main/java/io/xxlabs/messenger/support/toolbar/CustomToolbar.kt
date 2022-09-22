package io.xxlabs.messenger.support.toolbar

import android.text.Spanned
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.appContext

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
    override val enabled: Boolean = true,
    override val textColor: Int = getDefaultColors(enabled),
    override val iconTint: Int = R.color.neutral_active,
) : ToolbarMenuItem {

    override fun onClick(item: ToolbarMenuItem) {
        if (enabled) listener.onClick(item)
    }

    companion object {
        private val enabledTextColor: Int = appContext().getColor(R.color.brand_default)
        private val disabledTextColor: Int = appContext().getColor(R.color.neutral_disabled)

        fun getDefaultColors(enabled: Boolean): Int = if (enabled) enabledTextColor else disabledTextColor
    }
}