package io.xxlabs.messenger.bindings.wrapper.groups.member


class GroupMemberBindings(/*val groupMember: GroupMember*/): GroupMemberBase {
    override fun getDhKey(): ByteArray {
        TODO()
//        return groupMember.dhKey
    }

    override fun getID(): ByteArray {
        TODO()
//        return groupMember.id
    }

    override fun serialize(): ByteArray {
        TODO()
//        return groupMember.serialize()
    }

    override fun string(): String {
        TODO()
//        return groupMember.toString()
    }
}