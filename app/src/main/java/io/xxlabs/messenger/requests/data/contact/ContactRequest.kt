package io.xxlabs.messenger.requests.data.contact

import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.requests.model.ContactRequest

/**
 * Wrapper class for presenting a [Contact] as a friend request.
 */
data class ContactRequestData(
    override val model: Contact,
    override val unread: Boolean = false,
) : ContactRequest, Contact by model {
    override val requestId: ByteArray = model.userId
    override val name: String = model.displayName
    override val createdAt: Long = model.createdAt
    override val requestStatus: RequestStatus = RequestStatus.from(model.status)
}
