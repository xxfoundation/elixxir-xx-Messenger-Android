package io.xxlabs.messenger.requests.ui.list.adapter

interface RequestItemListener : ShowHiddenUI {
    fun onItemClicked(request: RequestItem)
    fun onActionClicked(request: RequestItem)
    fun markAsSeen(request: RequestItem)
}