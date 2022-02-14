package io.xxlabs.messenger.bindings.wrapper.report

import bindings.SendReport
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBindings

class SendReportBindings(val sendReport: SendReport): SendReportBase {
    override fun getMessageId(): ByteArray = sendReport.messageID

    override fun getRoundList(): RoundListBase = RoundListBindings(sendReport.roundList)

    override fun getTimestampMs(): Long = sendReport.timestampMS

    override fun getTimestampNano(): Long = sendReport.timestampNano

    override fun marshal(): ByteArray = sendReport.marshal()

    override fun getRoundUrl(): String = sendReport.roundURL
}