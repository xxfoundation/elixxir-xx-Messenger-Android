package io.xxlabs.messenger.bindings.wrapper.groups.chat

import io.elixxir.xxclient.groupchat.GroupChat
import io.elixxir.xxclient.models.BindingsModel.Companion.encodeArray
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
        return IdListBindings(groupChat.getGroups())
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
        val groupReport = groupChat.makeGroup(
            encodeArray(idList),
            name.encodeToByteArray(),
            initialMessage?.encodeToByteArray() ?: byteArrayOf()
        )

        return NewGroupReportBindings(groupReport, null)
    }

    override fun numGroups(): Long {
        return groupChat.numGroups
    }

    override fun resendRequest(groupIdBytes: ByteArray): NewGroupReportBase {
        val report = groupChat.resendRequest(groupIdBytes)
        return NewGroupReportBindings(report, groupChat.getGroup(groupIdBytes))
    }

    override fun send(groupIdBytes: ByteArray, message: ByteArray): GroupSendReportBase {
        return GroupSendReportBindings(groupChat.send(groupIdBytes, message, null))
    }
}