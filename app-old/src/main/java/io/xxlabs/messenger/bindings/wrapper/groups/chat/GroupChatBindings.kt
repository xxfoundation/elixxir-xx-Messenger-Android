package io.xxlabs.messenger.bindings.wrapper.groups.chat

import bindings.GroupChat
import bindings.IdList
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBindings
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBindings
import io.xxlabs.messenger.bindings.wrapper.groups.report.GroupSendReportBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.GroupSendReportBindings
import io.xxlabs.messenger.bindings.wrapper.groups.report.NewGroupReportBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.NewGroupReportBindings

class GroupChatBindings(val groupChat: GroupChat) : GroupChatBase {
    override fun getGroup(groupIdBytes: ByteArray): GroupBindings {
        return GroupBindings(groupChat.getGroup(groupIdBytes))
    }

    override fun getGroups(): IdListBase {
        return IdListBindings(groupChat.groups)
    }

    override fun joinGroup(serializedGroupData: ByteArray) {
        groupChat.joinGroup(serializedGroupData)
    }

    override fun leaveGroup(serializedGroupData: ByteArray) {
        groupChat.leaveGroup(serializedGroupData)
    }

    override fun makeGroup(
        idList: List<ByteArray>,
        name: String,
        initialMessage: String?
    ): NewGroupReportBase {
        val idListBindings = IdList()
        idList.forEach { id ->
            idListBindings.add(id)
        }

        val groupReport = groupChat.makeGroup(
            idListBindings,
            name.encodeToByteArray(),
            initialMessage?.encodeToByteArray() ?: byteArrayOf()
        )

        return NewGroupReportBindings(groupReport)
    }

    override fun numGroups(): Long {
        return groupChat.numGroups()
    }

    override fun resendRequest(groupIdBytes: ByteArray): NewGroupReportBase {
        val report = groupChat.resendRequest(groupIdBytes)
        return NewGroupReportBindings(report)
    }

    override fun send(groupIdBytes: ByteArray, message: ByteArray): GroupSendReportBase {
        return GroupSendReportBindings(groupChat.send(groupIdBytes, message))
    }
}