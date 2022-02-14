package io.xxlabs.messenger.data.room.model

import com.google.protobuf.ByteString
import data.proto.CMIXText
import data.proto.TextReply
import io.xxlabs.messenger.data.data.PayloadWrapper
import io.xxlabs.messenger.data.data.ReplyWrapper
import io.xxlabs.messenger.support.extensions.toBase64String

interface ChatMessage {
    var id: Long
    var uniqueId: ByteArray
    var status: Int
    var payload: String
    var timestamp: Long
    var unread: Boolean
    var sender: ByteArray
    var receiver: ByteArray
    var sendReport: ByteArray?
    val payloadWrapper: PayloadWrapper
    var roundUrl: String?

    override fun equals(other: Any?): Boolean

    companion object {
        fun buildCmixMsg(
            msg: String,
            replyTo: ReplyWrapper? = null
        ): String {
            val cmixReply = replyTo?.let { reply ->

                val replyBuilder = TextReply.newBuilder()
                    .setMessageId(ByteString.copyFrom(reply.uniqueId))
                    .setSenderId(ByteString.copyFrom(reply.senderId))
                replyBuilder.buildPartial()
            }

            val builder = CMIXText.newBuilder()
                .setText(msg)

            cmixReply?.let {
                builder.setReply(it)
            }

            return builder.buildPartial().toByteArray().toBase64String()
        }
    }
}