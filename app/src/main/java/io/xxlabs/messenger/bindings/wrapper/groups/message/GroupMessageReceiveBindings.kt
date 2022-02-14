package io.xxlabs.messenger.bindings.wrapper.groups.message

import bindings.GroupMessageReceive
import data.proto.CMIXText
import io.xxlabs.messenger.support.extensions.toBase64String

class GroupMessageReceiveBindings(val msg: GroupMessageReceive) : GroupMessageReceiveBase {
    override fun getEphemeralId(): Long {
        return msg.ephemeralID
    }

    override fun getGroupId(): ByteArray {
        return msg.groupID
    }

    override fun getMessageId(): ByteArray {
        return msg.messageID
    }

    override fun getPayload(): ByteArray {
        return msg.payload
    }

    override fun getPayloadString(): String? {
        if (msg.payload.contentEquals(byteArrayOf())) {
            return null
        }

        return CMIXText.parseFrom(msg.payload).toByteArray().toBase64String()
    }

    override fun getRecipientId(): ByteArray {
        return msg.recipientID
    }

    override fun getRoundId(): Long {
        return msg.roundID
    }

    override fun getSenderId(): ByteArray {
        return msg.senderID
    }

    override fun getRoundTimestampNano(): Long {
        return msg.roundTimestampNano
    }

    override fun getTimestampNano(): Long {
        return msg.timestampNano
    }

    override fun getTimestampMs(): Long {
        return msg.timestampMS
    }

    override fun getRoundUrl(): String? = msg.roundURL
}