package io.xxlabs.messenger.bindings.wrapper.groups.message

import io.xxlabs.messenger.data.room.model.GroupMessageData
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.util.Utils

class GroupMessageReceiveMock(val msg: GroupMessageData): GroupMessageReceiveBase {
    override fun getEphemeralId(): Long {
        return msg.id
    }

    override fun getGroupId(): ByteArray {
        return msg.uniqueId
    }

    override fun getMessageId(): ByteArray {
        return msg.uniqueId
    }

    override fun getPayload(): ByteArray {
        return msg.payload.fromBase64toByteArray()
    }

    override fun getPayloadString(): String {
        return msg.payload
    }

    override fun getRecipientId(): ByteArray {
        return msg.receiver
    }

    override fun getRoundId(): Long {
        return -1
    }

    override fun getRoundTimestampNano(): Long {
        return Utils.getCurrentTimeStampNano()
    }

    override fun getSenderId(): ByteArray {
        return msg.sender
    }

    override fun getTimestampNano(): Long {
        return Utils.getCurrentTimeStampNano()
    }

    override fun getTimestampMs(): Long {
        return Utils.getCurrentTimeStamp()
    }

    override fun getRoundUrl(): String = "https://www.google.com"
}