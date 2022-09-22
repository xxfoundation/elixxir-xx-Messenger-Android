package io.xxlabs.messenger.requests.ui.details.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.requests.model.GroupInvitation
import io.xxlabs.messenger.requests.ui.details.group.adapter.MemberItem
import io.xxlabs.messenger.requests.ui.list.adapter.GroupInviteItem

/**
 * [GroupInvitation] presentation logic.
 */
class InvitationDetails(
    private val item: GroupInviteItem,
    private val listener: InvitationDetailsListener,
    private val _isLoading: LiveData<Boolean>
) : InvitationDetailsUI {
    override val groupName: String = item.invite.name
    override val isLoading: LiveData<Boolean> by ::_isLoading

    override val positiveLabel: Int = R.string.invitation_details_positive_button
    override val negativeLabel: Int = R.string.invitation_details_negative_button
    override val positiveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    override fun onCloseClicked() {}

    override fun onPositiveClick() = listener.acceptInvitation(item.invite)

    override fun onNegativeClick() = listener.hideInvitation(item.invite)
}