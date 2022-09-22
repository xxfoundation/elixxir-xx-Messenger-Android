package io.xxlabs.messenger.requests.ui.accepted.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.requests.ui.accepted.RequestAcceptedUI

/**
 * Presentation logic for an accepted group invitation.
 */
class InvitationAccepted(
    private val group: Group,
    private val listener: InvitationAcceptedListener
) : RequestAcceptedUI {
    override val title: Int = R.string.invitation_accepted_title
    override val subtitle: String = group.name
    override val body: Int = R.string.invitation_accepted_body

    override val positiveLabel: Int = R.string.invitation_accepted_positive_button
    override val negativeLabel: Int = R.string.contact_accepted_negative_button
    override val positiveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    override fun onCloseClicked() {}
    override fun onPositiveClick() = listener.openGroupChat(group)
    override fun onNegativeClick() {}
}