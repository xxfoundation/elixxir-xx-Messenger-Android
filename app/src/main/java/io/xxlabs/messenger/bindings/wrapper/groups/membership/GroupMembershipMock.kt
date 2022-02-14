package io.xxlabs.messenger.bindings.wrapper.groups.membership

import io.xxlabs.messenger.bindings.wrapper.groups.member.GroupMemberBase
import io.xxlabs.messenger.bindings.wrapper.groups.member.GroupMemberMock

class GroupMembershipMock(val idsList: List<ByteArray>) : GroupMembershipBase {
    override fun get(i: Long): GroupMemberBase {
        return GroupMemberMock(idsList[i.toInt()])
    }

    override fun getAll(): List<GroupMemberBase> {
        val list = mutableListOf<GroupMemberMock>()
        for (i in idsList.indices) {
            val id = idsList[i]
            list.add(GroupMemberMock(id))
        }

        return list
    }

    override fun len(): Long {
        return idsList.size.toLong()
    }
}