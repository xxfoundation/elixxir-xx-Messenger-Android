package io.xxlabs.messenger.bindings.wrapper.groups.manager

import bindings.Bindings
import bindings.GroupMessageReceive
import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperBase
import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.groups.chat.GroupChatBase
import io.xxlabs.messenger.bindings.wrapper.groups.chat.GroupChatBindings
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBindings
import timber.log.Timber

class GroupsManagerBindings {
    companion object : GroupManagerBase {
        override fun initManager(
            client: ClientWrapperBase,
            onGroupReceived: (GroupBase) -> (Unit),
            onMessageReceived: (GroupMessageReceive) -> (Unit)
        ): GroupChatBase {
            client as ClientWrapperBindings
            val groupsManager = Bindings.newGroupManager(client.client, { group ->
                Timber.v("[GROUP MANAGER] Group received: $group")
                onGroupReceived(GroupBindings(group))
            }, { messageReceive ->
                Timber.v("[GROUP MANAGER] Message received: $messageReceive")
                onMessageReceived(messageReceive)
            })

            return GroupChatBindings(groupsManager)
        }
    }
}