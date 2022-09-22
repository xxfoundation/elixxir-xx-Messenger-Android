package io.xxlabs.messenger.bindings.wrapper.groups.member

interface GroupMemberBase {
    fun getDhKey(): ByteArray
    fun getID(): ByteArray
    fun serialize(): ByteArray
    fun string(): String
}