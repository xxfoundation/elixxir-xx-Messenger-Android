package io.xxlabs.messenger.requests.ui.accepted.group

import io.xxlabs.messenger.data.room.model.Group

interface InvitationAcceptedListener {
    fun openGroupChat(group: Group)
}