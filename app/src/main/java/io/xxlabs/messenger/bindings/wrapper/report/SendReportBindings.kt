package io.xxlabs.messenger.bindings.wrapper.report

import com.google.gson.Gson
import io.elixxir.xxclient.models.SendReport
import io.elixxir.xxclient.utils.fromBase64toByteArray
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBindings

class SendReportBindings(val sendReport: SendReport): SendReportBase {
    override fun getMessageId(): ByteArray = sendReport.messageId?.fromBase64toByteArray() ?: byteArrayOf()

    override fun getRoundList(): RoundListBase = RoundListBindings(sendReport.roundIdList)

    override fun getTimestampMs(): Long = (sendReport.timestamp ?: 0) / 1_000_000

    override fun getTimestampNano(): Long = sendReport.timestamp ?: 0

    override fun marshal(): ByteArray = Gson().toJson(sendReport).encodeToByteArray()

    override fun getRoundUrl(): String = sendReport.roundUrl ?: ""

    companion object {
        fun from(marshalledReport: ByteArray): SendReportBindings {
            return SendReportBindings(
                Gson().fromJson(marshalledReport.decodeToString(), SendReport::class.java)
            )
        }
    }
}