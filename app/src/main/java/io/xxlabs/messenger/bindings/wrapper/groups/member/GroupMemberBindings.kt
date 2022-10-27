package io.xxlabs.messenger.bindings.wrapper.groups.member

import com.google.gson.Gson
import io.elixxir.xxclient.models.BindingsModel.Companion.encode
import io.elixxir.xxclient.models.GroupMember


class GroupMemberBindings(val groupMember: GroupMember): GroupMemberBase {
    override fun getDhKey(): ByteArray {
        return encode(groupMember.dhKey)
    }

    override fun getID(): ByteArray {
        return groupMember.id
    }

    override fun serialize(): ByteArray {
        return encode(groupMember)
    }

    override fun string(): String {
        return Gson().toJson(groupMember)
    }
}