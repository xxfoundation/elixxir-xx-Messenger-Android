package io.xxlabs.messenger.data.room.model

interface GroupMessage : ChatMessage {
    var groupId: ByteArray
}