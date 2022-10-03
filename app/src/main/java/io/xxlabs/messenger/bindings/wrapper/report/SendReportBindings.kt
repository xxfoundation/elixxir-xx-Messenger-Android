package io.xxlabs.messenger.bindings.wrapper.report

import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBindings

class SendReportBindings(/*val sendReport: SendReport*/): SendReportBase {
    override fun getMessageId(): ByteArray = TODO("sendReport.messageID")

    override fun getRoundList(): RoundListBase = TODO("RoundListBindings(sendReport.roundList)")

    override fun getTimestampMs(): Long = TODO("sendReport.timestampMS")

    override fun getTimestampNano(): Long = TODO("sendReport.timestampNano")

    override fun marshal(): ByteArray = TODO("sendReport.marshal()")

    override fun getRoundUrl(): String = TODO("sendReport.roundURL")
}