package io.xxlabs.messenger.data.room.model

interface Group {
    val id: Long
    val groupId: ByteArray
    val name: String
    val leader: ByteArray
    val serial: ByteArray
    val status: Int

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}