package io.xxlabs.messenger.requests.bindings

import io.xxlabs.messenger.bindings.wrapper.groups.report.NewGroupReportBase
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.requests.model.GroupInvitation

interface GroupInvitationsService {
    // TODO: Return a Result<NewGroupReportBase>
    suspend fun createGroup(
        name: String,
        members: List<ContactData>,
        initialMessage: String?
    ): NewGroupReportBase?
    suspend fun joinGroup(group: GroupInvitation): Boolean
    suspend fun leaveGroup(group: Group): Boolean
    suspend fun resendInvitation(group: GroupInvitation): Boolean
}