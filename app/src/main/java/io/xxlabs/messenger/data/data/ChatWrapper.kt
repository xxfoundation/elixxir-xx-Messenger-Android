package io.xxlabs.messenger.data.data

import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData

data class ChatWrapper(
    val item: Any,
    var lastMessage: ChatMessage? = null,
    var unreadCount: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) return false

        other as ChatWrapper
        if (item is ContactData) {
            return other is ContactData && item.userId.contentEquals((other.item as ContactData).userId)
        } else if (item is GroupData) {
            return other is GroupData && item.groupId.contentEquals((other.item as GroupData).groupId)
        }

        return false
    }

    fun getTimestamp(): Long = lastMessage?.timestamp ?: 0

    fun getLastMessageSenderId(): ByteArray = lastMessage?.sender ?: byteArrayOf()

    fun getLastMessageId(): ByteArray = lastMessage?.uniqueId ?: byteArrayOf()

    fun setPhoto(newPhoto: ByteArray?) {
        if (item is ContactData) item.photo = newPhoto
    }

    fun updateName(displayName: String) {
        if (item is ContactData) item.nickname = displayName
    }

    fun isLastSenderContact(): Boolean = getLastMessageSenderId().contentEquals(getItemBindingsId())

    fun isNotEmpty(): Boolean = when (item) {
        is ContactData -> lastMessage != null
        is GroupData -> true
        else -> false
    }

    fun getItemId(): Long {
        if (item is ContactData) {
            return item.id
        } else if (item is GroupData) {
            return item.id + 10000
        }
        return -1L
    }

    fun getItemBindingsId(): ByteArray {
        if (item is ContactData) {
            return item.userId
        } else if (item is GroupData) {
            return item.groupId
        }
        return byteArrayOf()
    }

    override fun hashCode(): Int {
        return item.hashCode()
    }

    fun isAccepted(): Boolean {
        return when (item) {
            is ContactData -> {
                item.status == RequestStatus.ACCEPTED.value
            }
            is GroupData -> {
                item.status == RequestStatus.ACCEPTED.value
            }
            else -> {
                false
            }
        }
    }
}