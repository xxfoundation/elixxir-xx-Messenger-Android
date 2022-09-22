package io.xxlabs.messenger.data.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import timber.log.Timber

data class ReplyWrapper(
    @SerializedName("senderId") val senderId: ByteArray,
    @SerializedName("messageId") val uniqueId: ByteArray,
) {
    companion object {
        fun getInstance(replyPayload: String): ReplyWrapper {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val json = JsonParser
                .parseString(replyPayload)
                .asJsonObject

            return gson.fromJson(json, ReplyWrapper::class.java)
        }

        fun buildJsonInstance(
            senderId: ByteArray,
            uniqueId: ByteArray,
        ): String {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val replyWrapper = ReplyWrapper(
                senderId,
                uniqueId,
            )

            val jsonString = gson.toJson(replyWrapper)
            Timber.v("Preview reply: $jsonString")
            return jsonString
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReplyWrapper

        if (!senderId.contentEquals(other.senderId)) return false
        if (!uniqueId.contentEquals(other.uniqueId)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = senderId.contentHashCode()
        result = 31 * result + uniqueId.contentHashCode()
        return result
    }
}