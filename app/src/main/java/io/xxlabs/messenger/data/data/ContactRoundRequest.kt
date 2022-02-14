package io.xxlabs.messenger.data.data

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.xxlabs.messenger.data.datatype.ContactRequestState
import io.xxlabs.messenger.support.util.Utils
import timber.log.Timber

data class ContactRoundRequest(
    @SerializedName("contactId") val contactId: ByteArray,
    @SerializedName("contactUsername") val contactUsername: String,
    @SerializedName("roundId")  val roundId: Long,
    @SerializedName("requestTimestamp")  var requestTimestamp: Long = Utils.getCurrentTimeStamp(),
    @SerializedName("isSent") val isSent: Boolean,
    @SerializedName("verifyState")  var verifyState: ContactRequestState = ContactRequestState.VERIFYING
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) return false

        other as ContactRoundRequest
        return contactId.contentEquals(other.contactId) && roundId == other.roundId
    }

    override fun hashCode(): Int {
        var result = contactId.hashCode()
        result = 31 * result + roundId.hashCode()
        return result
    }

    fun toGson(): String {
        return gson.toJson(this)
    }

    companion object {
        private val gson = GsonBuilder().setLenient().create()

        fun getInstance(jsonString: String): ContactRoundRequest {
            return gson.fromJson(jsonString, ContactRoundRequest::class.java)
        }

        fun toRoundRequestsSet(jsonList: Set<String>): MutableSet<ContactRoundRequest> {
            val roundsList = mutableSetOf<ContactRoundRequest>()
            jsonList.forEach { json ->
                try {
                    roundsList.add(getInstance(json))
                } catch (err: Exception) {
                    Timber.e("Error parsing json to element: ${err.localizedMessage}")
                }
            }

            return roundsList
        }

        fun toJsonSet(contactRoundRequestList: Set<ContactRoundRequest>): MutableSet<String> {
            val jsonRoundsList = mutableSetOf<String>()
            contactRoundRequestList.forEach { roundRequest ->
                try {
                    jsonRoundsList.add(roundRequest.toGson())
                } catch (err: Exception) {
                    Timber.e("Error parsing element to json: ${err.localizedMessage}")
                }
            }

            return jsonRoundsList
        }
    }
}