package io.xxlabs.messenger.requests.data.group

import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.requests.model.GroupInvitation

/**
 * Wrapper class for presenting a [Group] as an invitation.
 */
data class GroupInvitationData(
    override val model: Group,
    override val unread: Boolean = false,
) : GroupInvitation, Group by model {
    override val requestId: ByteArray = model.groupId
    override val name: String = model.name
    override val requestStatus: RequestStatus = RequestStatus.from(model.status)
    override val createdAt: Long = System.currentTimeMillis()
}
