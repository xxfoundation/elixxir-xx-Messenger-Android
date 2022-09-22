package io.xxlabs.messenger.requests.bindings

import io.xxlabs.messenger.bindings.wrapper.groups.report.NewGroupReportBase
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.requests.model.GroupInvitation
import io.xxlabs.messenger.support.util.value
import javax.inject.Inject

class BindingsInvitationsMediator @Inject constructor(
    private val repo: BaseRepository
) : GroupInvitationsService {

    override suspend fun createGroup(
        name: String,
        members: List<ContactData>,
        initialMessage: String?
    ): NewGroupReportBase? {
        val memberIds = members.map { it.userId }
        return try {
            repo.makeGroup(
                name,
                memberIds,
                initialMessage
            ).value()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun joinGroup(group: GroupInvitation): Boolean {
        return true
    }

    override suspend fun leaveGroup(group: Group): Boolean {
        return true
    }

    override suspend fun resendInvitation(group: GroupInvitation): Boolean {
        return true
    }
}