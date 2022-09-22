package io.xxlabs.messenger.bindings.wrapper.groups.membership

import bindings.GroupMembership
import io.xxlabs.messenger.bindings.wrapper.groups.member.GroupMemberBase
import io.xxlabs.messenger.bindings.wrapper.groups.member.GroupMemberBindings

class GroupMembershipBindings(val groupMembership: GroupMembership) : GroupMembershipBase {
    override fun get(i: Long): GroupMemberBase {
        val groupMember = groupMembership.get(i)
        return GroupMemberBindings(groupMember)
    }

    override fun getAll(): List<GroupMemberBase> {
        val list = mutableListOf<GroupMemberBindings>()
        for (i in 0 until len()) {
            list.add(GroupMemberBindings(groupMembership[i]))
        }
        return list
    }

    override fun len(): Long {
        return groupMembership.len()
    }
}