package io.xxlabs.messenger.data.room.model

interface Contact {
    val id: Long
    val userId: ByteArray
    val username: String
    val status: Int
    val nickname: String
    val photo: ByteArray?
    val email: String
    val phone: String
    val marshaled: ByteArray?
    val createdAt: Long
    val displayName: String
    val initials: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    fun hasFacts(): Boolean
}