package io.xxlabs.messenger.data.room.model

interface PrivateMessage : ChatMessage {
    val fileType: String?
    val fileUri: String?
    val transferProgress: Long
}