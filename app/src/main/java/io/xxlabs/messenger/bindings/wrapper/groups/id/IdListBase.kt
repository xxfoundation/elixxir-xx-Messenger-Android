package io.xxlabs.messenger.bindings.wrapper.groups.id

interface IdListBase {
    fun add(id: ByteArray)
    fun get(id: Long): ByteArray
    fun len(): Long
}