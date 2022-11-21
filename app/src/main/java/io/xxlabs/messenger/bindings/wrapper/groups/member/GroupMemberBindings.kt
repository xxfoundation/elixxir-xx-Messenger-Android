package io.xxlabs.messenger.bindings.wrapper.groups.member

import com.google.gson.Gson
import io.elixxir.xxclient.models.BindingsModel.Companion.encode
import io.elixxir.xxclient.models.GroupMember
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray


class GroupMemberBindings(val groupMember: GroupMember): GroupMemberBase {
    override fun getDhKey(): ByteArray {
        return byteArrayOf()
    }

    override fun getID(): ByteArray {
        return groupMember.id.fromBase64toByteArray()
    }

    override fun serialize(): ByteArray {
        return encode(groupMember)
    }

    override fun string(): String {
        return Gson().toJson(groupMember)
    }
}