package io.xxlabs.messenger.data.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import timber.log.Timber

data class NetworkPreviewWrapper(
    @SerializedName("title") val title: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("image") val image: String = ""
) {
    companion object {
        private val gson = GsonBuilder().setLenient().create()

        fun getInstance(urlJson: String): NetworkPreviewWrapper {
            val json = JsonParser
                .parseString(urlJson)
                .asJsonObject

            return gson.fromJson(json, NetworkPreviewWrapper::class.java)
        }

        fun buildJsonString(
            title: String,
            url: String,
            image: String
        ): String {
            val urlJsonPreview = NetworkPreviewWrapper(
                title,
                url,
                image
            )

            val jsonString = gson.toJson(urlJsonPreview)
            Timber.v("Preview: $jsonString")
            return jsonString
        }
    }

    override fun toString(): String {
        val urlJsonPreview = NetworkPreviewWrapper(
            title,
            url
        )

        val jsonString = gson.toJson(urlJsonPreview)
        Timber.v("Preview: $jsonString")
        return jsonString
    }
}