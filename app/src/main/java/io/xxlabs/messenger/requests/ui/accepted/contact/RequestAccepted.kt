package io.xxlabs.messenger.requests.ui.accepted.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.requests.ui.accepted.RequestAcceptedUI

/**
 * Presentation logic for an accepted request.
 */
class RequestAccepted(
    private val contact: Contact,
    private val listener: RequestAcceptedListener
) : RequestAcceptedUI {
    override val title: Int = R.string.request_accepted_title
    override val subtitle: String = contact.displayName
    override val body: Int = R.string.request_accepted_body

    override val positiveLabel: Int = R.string.contact_accepted_positive_button
    override val negativeLabel: Int = R.string.contact_accepted_negative_button
    override val positiveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    override fun onCloseClicked() {}
    override fun onPositiveClick() = listener.sendMessage(contact)
    override fun onNegativeClick() {}
}