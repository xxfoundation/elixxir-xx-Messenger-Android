package io.xxlabs.messenger.bindings.wrapper.groups.membership

import io.xxlabs.messenger.bindings.wrapper.groups.member.GroupMemberBase

interface GroupMembershipBase {
    operator fun get(i: Long): GroupMemberBase
    fun len(): Long
    fun getAll(): List<GroupMemberBase>
}