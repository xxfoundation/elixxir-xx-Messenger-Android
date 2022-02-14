package io.xxlabs.messenger.data.room.model

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import data.proto.CMIXText
import io.xxlabs.messenger.data.data.PayloadWrapper
import io.xxlabs.messenger.data.data.ReplyWrapper
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.isMockVersion

abstract class XxChatMessage : ChatMessage {

    override val payloadWrapper: PayloadWrapper
        get() {
            return if (isMockVersion()) getMockPayloadWrapper()
            else getPayloadWrapperFromCmix()
        }

    private fun getMockPayloadWrapper(): PayloadWrapper {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val json = JsonParser
            .parseString(payload)
            .asJsonObject

        return gson.fromJson(json, PayloadWrapper::class.java)
    }

    private fun getPayloadWrapperFromCmix(): PayloadWrapper {
        val cmixPayload = getCmixWrapper()
        val replyCmix = cmixPayload.reply
        val payloadWrapper = PayloadWrapper(cmixPayload.text)

        if (!replyCmix.messageId.isEmpty && !replyCmix.senderId.isEmpty) {
            val replyWrapper = ReplyWrapper(
                replyCmix.senderId.toByteArray(),
                replyCmix.messageId.toByteArray()
            )

            payloadWrapper.reply = replyWrapper
        }

        return payloadWrapper
    }

    private fun getCmixWrapper(): CMIXText {
        return CMIXText.parseFrom(payload.fromBase64toByteArray())
    }
}