package io.xxlabs.messenger.bindings.wrapper.groups.membership

import io.elixxir.xxclient.models.GroupMember
import io.xxlabs.messenger.bindings.wrapper.groups.member.GroupMemberBase
import io.xxlabs.messenger.bindings.wrapper.groups.member.GroupMemberBindings

class GroupMembershipBindings(private val groupMembership: List<GroupMember>) : GroupMembershipBase {
    override fun get(i: Long): GroupMemberBase {
        val groupMember = groupMembership[i.toInt()]
        return GroupMemberBindings(groupMember)
    }

    override fun getAll(): List<GroupMemberBase> {
        val list = mutableListOf<GroupMemberBindings>()
        for (i in 0 until len()) {
            list.add(GroupMemberBindings(groupMembership[i.toInt()]))
        }
        return list
    }

    override fun len(): Long {
        return groupMembership.size.toLong()
    }
}