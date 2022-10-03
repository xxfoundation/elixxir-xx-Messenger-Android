package io.xxlabs.messenger.bindings.wrapper.groups.message

import data.proto.CMIXText
import io.xxlabs.messenger.support.extensions.toBase64String

class GroupMessageReceiveBindings(/*val msg: GroupMessageReceive*/) : GroupMessageReceiveBase {
    override fun getEphemeralId(): Long {
        TODO()
//        return msg.ephemeralID
    }

    override fun getGroupId(): ByteArray {
        TODO()
//        return msg.groupID
    }

    override fun getMessageId(): ByteArray {
        TODO()
//        return msg.messageID
    }

    override fun getPayload(): ByteArray {
        TODO()
//        return msg.payload
    }

    override fun getPayloadString(): String? {
        TODO()
//        if (msg.payload.contentEquals(byteArrayOf())) {
//            return null
//        }
//
//        return CMIXText.parseFrom(msg.payload).toByteArray().toBase64String()
    }

    override fun getRecipientId(): ByteArray {
        TODO()
//        return msg.recipientID
    }

    override fun getRoundId(): Long {
        TODO()
//        return msg.roundID
    }

    override fun getSenderId(): ByteArray {
        TODO()
//        return msg.senderID
    }

    override fun getRoundTimestampNano(): Long {
        TODO()
//        return msg.roundTimestampNano
    }

    override fun getTimestampNano(): Long {
        TODO()
//        return msg.timestampNano
    }

    override fun getTimestampMs(): Long {
        TODO()
//        return msg.timestampMS
    }

    override fun getRoundUrl(): String? = TODO("msg.roundURL")
}