package io.xxlabs.messenger.bindings.wrapper.groups.chat

import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.GroupSendReportBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.NewGroupReportBase

interface GroupChatBase {
    fun getGroup(groupIdBytes: ByteArray): GroupBase
    fun getGroups(): IdListBase
    fun joinGroup(serializedGroupData: ByteArray)
    fun leaveGroup(serializedGroupData: ByteArray)
    fun makeGroup(
        idList: List<ByteArray>,
        name: String,
        initialMessage: String?
    ): NewGroupReportBase

    fun numGroups(): Long
    fun resendRequest(groupIdBytes: ByteArray): NewGroupReportBase
    fun send(groupIdBytes: ByteArray, message: ByteArray): GroupSendReportBase
}