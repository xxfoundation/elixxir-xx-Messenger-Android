package io.xxlabs.messenger.bindings.wrapper.groups.message

interface GroupMessageReceiveBase {
     fun getEphemeralId(): Long
     fun getGroupId(): ByteArray
     fun getMessageId(): ByteArray
     fun getPayload(): ByteArray
     fun getPayloadString(): String?
     fun getRecipientId(): ByteArray
     fun getSenderId(): ByteArray
     fun getRoundId(): Long
     fun getRoundTimestampNano(): Long
     fun getTimestampNano(): Long
     fun getTimestampMs(): Long
     fun getRoundUrl(): String?
}