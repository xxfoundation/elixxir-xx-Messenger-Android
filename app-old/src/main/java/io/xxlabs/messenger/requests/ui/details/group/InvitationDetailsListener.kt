package io.xxlabs.messenger.requests.ui.details.group

import io.xxlabs.messenger.requests.model.GroupInvitation

interface InvitationDetailsListener {
    fun acceptInvitation(invitation: GroupInvitation)
    fun hideInvitation(invitation: GroupInvitation)
}