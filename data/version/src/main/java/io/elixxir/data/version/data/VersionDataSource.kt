package io.elixxir.data.version.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.elixxir.data.version.VersionRepository
import io.elixxir.data.version.model.*
import io.elixxir.data.version.model.VersionData

internal class VersionDataSource : VersionRepository {

    override suspend fun checkVersion(): Result<VersionState> {
        return try {
            Result.success(
                parse(downloadRegistrationJson())
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchErrorJson(): String {
        TODO("Not yet implemented")
    }

    private fun downloadRegistrationJson(): JsonObject {
        TODO("Use Retrofit for the web call.")
    }

    private fun parse(json: JsonElement): VersionState {
        val registrationWrapper = VersionData.from(json)
        val appVersion = registrationWrapper.appVersion
        val minVersion = registrationWrapper.minVersion
        val recommendedVersion = registrationWrapper.recommendedVersion
        val downloadUrl = registrationWrapper.downloadUrl
        val popupMessage = registrationWrapper.minPopupMessage

        return when {
            appVersion < minVersion -> UpdateRequired(popupMessage, downloadUrl)
            appVersion >= minVersion && appVersion < recommendedVersion -> {
                UpdateRecommended(downloadUrl)
            }
            else -> VersionOk
        }
    }
}