package io.xxlabs.messenger.requests.model

import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.Group
import java.io.Serializable

sealed interface Request : Serializable {
    val requestId: ByteArray
    val name: String
    val createdAt: Long
    val requestStatus: RequestStatus
    val unread: Boolean

    override fun equals(other: Any?): Boolean
}

interface ContactRequest : Request {
    val model: Contact
}

interface GroupInvitation : Request {
    val model: Group
}

class NullRequest : Request {
    override val requestId: ByteArray get() = byteArrayOf()
    override val name: String get() = ""
    override val createdAt: Long get() = 0
    override val requestStatus: RequestStatus get() = RequestStatus.VERIFIED
    override val unread: Boolean get() = false
    override fun equals(other: Any?): Boolean = other is NullRequest
}

private data class DummyRequest(val position: Int) : Request {
    override val requestId: ByteArray = position.toString().toByteArray()
    override val name: String = "Request #$position"
    override val createdAt: Long = System.currentTimeMillis()
    override val requestStatus: RequestStatus = RequestStatus.SENT
    override val unread: Boolean = true
}

data class DummyContactRequest(
    val position: Int,
    val request: Request = DummyRequest(position)
) : ContactRequest, Request by request {
    override val model: Contact = DummyContact(position)
    override val requestStatus: RequestStatus = RequestStatus.from(position % 8)
}

data class DummyContact(
    val position: Int
) : Contact {
    override val id: Long = position.toLong()
    override val userId: ByteArray = position.toString().toByteArray()
    override val username: String = "User $position"
    override val status: Int = RequestStatus.SENT.value
    override val nickname: String = username
    override val photo: ByteArray? = null
    override val email: String = "user${position}@gmail.com"
    override val phone: String = "+0 123-456-7890"
    override val marshaled: ByteArray? = null
    override val createdAt: Long = System.currentTimeMillis()
    override val displayName: String = username
    override val initials: String = "KB"

    override fun hasFacts(): Boolean =  false
}

data class DummyGroupRequest(
    val position: Int,
    val request: Request = DummyRequest(position + 100)
) : GroupInvitation, Request by request {
    override val model: Group = DummyGroup(position + 100)
    override val requestStatus: RequestStatus = RequestStatus.SENT
}

data class DummyGroup(
    val position: Int
) : Group {
    override val id: Long = position.toLong()
    override val groupId: ByteArray = position.toString().toByteArray()
    override val name: String = "Group $position"
    override val leader: ByteArray = byteArrayOf()
    override val serial: ByteArray = byteArrayOf()
    override val status: Int = RequestStatus.VERIFIED.value
}