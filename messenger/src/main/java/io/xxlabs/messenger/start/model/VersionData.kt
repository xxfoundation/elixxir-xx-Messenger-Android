package io.xxlabs.messenger.start.model

import com.google.gson.JsonElement
import io.xxlabs.messenger.BuildConfig

data class VersionData(
    val cmixOldest: String = "",
    val cmixLatest: String = "",
    val minVersion: Double = 1.0,
    val appVersion: Double = if (BuildConfig.DEBUG) 100.0 else BuildConfig.APP_VERSION,
    val recommendedVersion: Double = 1.0,
    val downloadUrl: String = "",
    val minPopupMessage: String = "",
) {
    companion object {
        fun from(jsonElement: JsonElement): VersionData {
            val cmix = jsonElement.asJsonObject["cmix-client"].asJsonObject
            val dappId = jsonElement.asJsonObject["dapp-id"].asJsonObject

            return VersionData(
                cmixOldest = cmix["oldest"].asString,
                cmixLatest = cmix["latest"].asString,
                minVersion = dappId["new_android_min_version"].asDouble,
                recommendedVersion = dappId["new_android_recommended_version"].asDouble,
                downloadUrl = dappId["new_android_app_url"].asString,
                minPopupMessage = dappId["new_minimum_popup_msg"].asString
            )
        }
    }
}