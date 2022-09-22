package io.xxlabs.messenger.requests.ui.nickname

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.requests.ui.send.OutgoingRequest

class SaveNickname(
    private val outgoingRequest: OutgoingRequest,
    private val listener: SaveNicknameListener
): SaveNicknameUI {
    override val nicknameHint: LiveData<String> by ::_nicknameHint
    private val _nicknameHint = MutableLiveData(outgoingRequest.receiver.displayName)

    override val nicknameError: LiveData<String?> by ::_nicknameError
    private val _nicknameError = MutableLiveData<String?>(null)
    override val maxNicknameLength: Int = 32
    private var nickname: String? = null

    override val positiveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    override fun onNicknameInput(editable: Editable) {
        nickname = editable.toString()
        with (editable) {
            _nicknameHint.value = if (isEmpty()) outgoingRequest.receiver.displayName else "Nickname"
            _nicknameError.value =
                if (isNotEmpty() && isBlank()) "Cannot be blank."
                else null
        }
    }
    override fun onPositiveClick() = listener.saveNickname(outgoingRequest, nickname)

    override fun onCloseClicked() {}
}