package io.xxlabs.messenger.bindings.wrapper.groups.report

import bindings.GroupSendReport

class GroupSendReportBindings(private val groupSendReport: GroupSendReport) : GroupSendReportBase {
    override fun getRoundID(): Long = groupSendReport.roundID

    override fun getTimestampMs(): Long = groupSendReport.timestampMS

    override fun getMessageID(): ByteArray = groupSendReport.messageID

    override fun getRoundUrl(): String? = groupSendReport.roundURL
}