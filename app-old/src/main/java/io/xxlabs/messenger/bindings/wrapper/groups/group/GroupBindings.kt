package io.xxlabs.messenger.bindings.wrapper.groups.group

import bindings.Group
import io.xxlabs.messenger.bindings.wrapper.groups.membership.GroupMembershipBindings

class GroupBindings(val group: Group): GroupBase {
    override fun getID(): ByteArray {
        return group.id
    }

    override fun getMembership(): GroupMembershipBindings {
        return GroupMembershipBindings(group.membership)
    }

    override fun getName(): ByteArray {
        return group.name
    }

    override fun serialize(): ByteArray {
        return group.serialize()
    }

    override fun initMessage(): String? {
        return group.initMessage?.decodeToString()
    }

    override fun getCreatedMs(): Long {
        return group.createdMS
    }
}