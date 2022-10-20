package io.xxlabs.messenger.bindings.wrapper.report

import io.elixxir.xxclient.models.SendReport
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBindings

class SendReportBindings(val sendReport: SendReport): SendReportBase {
    override fun getMessageId(): ByteArray = sendReport.messageId ?: byteArrayOf()

    override fun getRoundList(): RoundListBase = RoundListBindings(sendReport.roundIdList)

    override fun getTimestampMs(): Long = sendReport.timestamp ?: 0

    override fun getTimestampNano(): Long = (sendReport.timestamp ?: 0) * 1_000_000

    override fun marshal(): ByteArray = byteArrayOf()

    override fun getRoundUrl(): String = sendReport.roundUrl ?: ""
}