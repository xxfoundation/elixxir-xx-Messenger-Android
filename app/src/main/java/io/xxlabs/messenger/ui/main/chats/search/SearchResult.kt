package io.xxlabs.messenger.ui.main.chats.search

import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.data.room.model.thumbnail
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail

sealed interface SearchResultItem {
    val id: Int
}

data class SectionHeader(
    val label: String,
    override val id: Int = label.hashCode()
) : SearchResultItem

sealed interface SearchResult : SearchResultItem {
    val thumbnail: ItemThumbnail
    val name: String
    val description: String?
    val timestamp: String?
    val notificationCount: String?
    fun onClick()

    override fun equals(other: Any?): Boolean
}

data class ConnectionResult(
    private val listener: SearchResultListener,
    val model: Contact,
    override val thumbnail: ItemThumbnail,
) : SearchResult {
    override val id: Int = model.userId.hashCode()
    override val name: String = model.displayName
    override val description: String = model.username
    override val timestamp: String? = null
    override val notificationCount: String? = null

    override fun onClick() = listener.onConnectionClicked(model)
}

data class PrivateChatResult(
    private val listener: SearchResultListener,
    val model: Contact,
    private val lastMessage: String,
    override val thumbnail: ItemThumbnail,
    override val timestamp: String?,
    override val notificationCount: String?,
) : SearchResult {
    override val id: Int = model.userId.hashCode()
    override val name: String = model.displayName
    override val description: String = lastMessage

    override fun onClick() = listener.onConnectionClicked(model)
}

data class GroupChatResult(
    private val listener: SearchResultListener,
    val model: Group,
    private val lastMessage: String,
    override val timestamp: String,
    override val notificationCount: String?,
) : SearchResult {
    override val id: Int = model.groupId.hashCode()
    override val thumbnail: ItemThumbnail = model.thumbnail
    override val name: String = model.name
    override val description: String = lastMessage

    override fun onClick() = listener.onGroupChatClicked(model)
}

