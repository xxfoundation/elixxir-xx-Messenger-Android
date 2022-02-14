package io.xxlabs.messenger.data.data

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.io.Serializable

data class PayloadWrapper(
    @SerializedName("text") val text: String,
    @SerializedName("reply") var reply: ReplyWrapper? = null
): Serializable {
    override fun toString(): String {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val json = gson.toJsonTree(this)
        val jsonString = gson.toJson(json)
        Timber.v("Msg string preview: $jsonString")
        return jsonString
    }
}