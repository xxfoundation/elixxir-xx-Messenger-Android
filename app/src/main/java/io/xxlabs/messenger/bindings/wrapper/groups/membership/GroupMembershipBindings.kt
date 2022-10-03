package io.xxlabs.messenger.bindings.wrapper.groups.membership

import io.xxlabs.messenger.bindings.wrapper.groups.member.GroupMemberBase
import io.xxlabs.messenger.bindings.wrapper.groups.member.GroupMemberBindings

class GroupMembershipBindings(/*val groupMembership: GroupMembership*/) : GroupMembershipBase {
    override fun get(i: Long): GroupMemberBase {
        TODO()
//        val groupMember = groupMembership.get(i)
//        return GroupMemberBindings(groupMember)
    }

    override fun getAll(): List<GroupMemberBase> {
        TODO()
//        val list = mutableListOf<GroupMemberBindings>()
//        for (i in 0 until len()) {
//            list.add(GroupMemberBindings(groupMembership[i]))
//        }
//        return list
    }

    override fun len(): Long {
        TODO()
//        return groupMembership.len()
    }
}