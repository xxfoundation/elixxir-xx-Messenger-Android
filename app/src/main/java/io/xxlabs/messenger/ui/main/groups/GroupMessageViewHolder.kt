package io.xxlabs.messenger.ui.main.groups

import android.view.View
import io.xxlabs.messenger.data.data.ReplyWrapper
import io.xxlabs.messenger.data.room.model.GroupMessage
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageViewHolder

abstract class GroupMessageViewHolder(parent: View) : MessageViewHolder<GroupMessage>(parent) {

    protected lateinit var listener: GroupMessageListener<GroupMessage>
    protected lateinit var username: String

    override fun onBind(
        message: GroupMessage,
        listener: MessageListener<GroupMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<GroupMessage>
    ) {
        this.listener = listener as GroupMessageListener<GroupMessage>
        val userId = UserId(message.sender.decodeToString())
        username = listener.membersList[userId].value

        super.onBind(message, listener, header, selectionMode, chatViewModel)
    }

    override fun replyUsername(reply: ReplyWrapper): String {
        val userId = UserId.from(reply.senderId)
        return listener.membersList[userId].value
    }
}

/**
 * Receives UI events from an item displayed in a list.
 */
interface GroupMessageListener<T: GroupMessage>: MessageListener<T> {
   val membersList: UserIdToUsernameMap
}