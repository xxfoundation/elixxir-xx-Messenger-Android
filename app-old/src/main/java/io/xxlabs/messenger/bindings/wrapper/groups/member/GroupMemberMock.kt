package io.xxlabs.messenger.bindings.wrapper.groups.member

import java.util.*

class GroupMemberMock(val id: ByteArray) : GroupMemberBase {
    override fun getDhKey(): ByteArray {
        return UUID.randomUUID().toString().encodeToByteArray() + id
    }

    override fun getID(): ByteArray {
        return id
    }

    override fun serialize(): ByteArray {
        return byteArrayOf()
    }

    override fun string(): String {
        return ""
    }
}