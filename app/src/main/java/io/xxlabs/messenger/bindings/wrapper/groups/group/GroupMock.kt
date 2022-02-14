package io.xxlabs.messenger.bindings.wrapper.groups.group

import io.xxlabs.messenger.bindings.wrapper.groups.membership.GroupMembershipMock
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.support.util.Utils

class GroupMock(val group: GroupData, val idsList: List<ByteArray>) : GroupBase {
    override fun getID(): ByteArray {
        return group.groupId
    }

    override fun getMembership(): GroupMembershipMock {
        return GroupMembershipMock(idsList)
    }

    override fun getName(): ByteArray {
        return group.name.encodeToByteArray()
    }

    override fun serialize(): ByteArray {
        return group.serial
    }

    override fun initMessage(): String {
        return "Welcome to ${getName()}"
    }

    override fun getCreatedMs(): Long {
        return Utils.getCurrentTimeStamp()
    }
}