package io.xxlabs.messenger.requests.ui.nickname

import io.xxlabs.messenger.requests.ui.send.OutgoingRequest

interface SaveNicknameListener {
    fun saveNickname(request: OutgoingRequest, nickname: String?)
}