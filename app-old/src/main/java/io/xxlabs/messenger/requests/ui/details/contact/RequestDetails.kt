package io.xxlabs.messenger.requests.ui.details.contact

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.formattedEmail
import io.xxlabs.messenger.data.room.model.formattedPhone
import io.xxlabs.messenger.requests.model.ContactRequest

/**
 * [ContactRequest] presentation logic.
 */
class RequestDetails(
    private val request: ContactRequest,
    private val listener: RequestDetailsListener
) : RequestDetailsUI {
    override val username: String = request.model.displayName
    override val email: String? =  request.model.formattedEmail()
    override val phone: String? = request.model.formattedPhone()

    override val nicknameHint: LiveData<String> by ::_nicknameHint
    private val _nicknameHint = MutableLiveData(username)

    override val nicknameError: LiveData<String?> by ::_nicknameError
    private val _nicknameError = MutableLiveData<String?>(null)
    override val maxNicknameLength: Int = 32

    override val positiveLabel: Int = R.string.request_details_positive_button
    override val negativeLabel: Int = R.string.request_details_negative_button
    override val positiveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    private var nickname: String? = null

    override fun onNicknameInput(editable: Editable) {
        nickname = editable.toString()
        with (editable) {
            _nicknameHint.value = if (isEmpty()) username else "Nickname"
            _nicknameError.value =
                if (isNotEmpty() && isBlank()) "Cannot be blank."
                else null
        }
    }

    override fun onCloseClicked() {}

    override fun onPositiveClick() = listener.acceptRequest(request, nickname)

    override fun onNegativeClick() = listener.hideRequest(request)
}