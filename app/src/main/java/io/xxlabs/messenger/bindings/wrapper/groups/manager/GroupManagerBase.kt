package io.xxlabs.messenger.bindings.wrapper.groups.manager

import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperBase
import io.xxlabs.messenger.bindings.wrapper.groups.chat.GroupChatBase
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase

interface GroupManagerBase {
    fun initManager(
        client: ClientWrapperBase,
        onGroupReceived: (GroupBase) -> (Unit),
        onMessageReceived: (/*GroupMessageReceive*/) -> (Unit)
    ): GroupChatBase
}