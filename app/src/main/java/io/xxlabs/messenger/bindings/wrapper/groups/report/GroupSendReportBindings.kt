package io.xxlabs.messenger.bindings.wrapper.groups.report

import bindings.GroupSendReport

class GroupSendReportBindings(private val groupSendReport: GroupSendReport) : GroupSendReportBase {
    override fun getRoundID(): Long = TODO("groupSendReport.roundID")

    override fun getTimestampMs(): Long = TODO("groupSendReport.timestampMS")

    override fun getMessageID(): ByteArray = TODO("groupSendReport.messageID")

    override fun getRoundUrl(): String? = TODO("groupSendReport.roundURL")
}