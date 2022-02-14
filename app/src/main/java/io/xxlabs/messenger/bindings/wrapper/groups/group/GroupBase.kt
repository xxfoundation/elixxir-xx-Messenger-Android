package io.xxlabs.messenger.bindings.wrapper.groups.group

import io.xxlabs.messenger.bindings.wrapper.groups.membership.GroupMembershipBase

interface GroupBase {
    fun getID(): ByteArray
    fun getMembership(): GroupMembershipBase
    fun getName(): ByteArray
    fun serialize(): ByteArray
    fun initMessage(): String?
    fun getCreatedMs(): Long
}