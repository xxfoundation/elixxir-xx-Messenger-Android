package io.xxlabs.messenger.bindings.wrapper.groups.member

import bindings.GroupMember

class GroupMemberBindings(val groupMember: GroupMember): GroupMemberBase {
    override fun getDhKey(): ByteArray {
        return groupMember.dhKey
    }

    override fun getID(): ByteArray {
        return groupMember.id
    }

    override fun serialize(): ByteArray {
        return groupMember.serialize()
    }

    override fun string(): String {
        return groupMember.toString()
    }
}