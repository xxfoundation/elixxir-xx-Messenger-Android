package io.xxlabs.messenger.support.toolbar

import android.text.Spanned

interface ToolbarUI {
    val title: Spanned
    val menuItems: List<ToolbarMenuItem>
    fun onBackPressed()
}

interface ToolbarMenuItem {
    val id: Int
    val icon: Int?
    val iconTint: Int
    val label: Int?
    val textColor: Int
    val enabled: Boolean
    fun onClick(item: ToolbarMenuItem)

    override fun equals(other: Any?): Boolean
}