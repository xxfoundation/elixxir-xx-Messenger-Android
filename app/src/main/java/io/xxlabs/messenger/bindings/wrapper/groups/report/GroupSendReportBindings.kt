package io.xxlabs.messenger.bindings.wrapper.groups.report

import io.elixxir.xxclient.models.GroupSendReport


class GroupSendReportBindings(private val groupSendReport: GroupSendReport?) : GroupSendReportBase {
    override fun getRoundID(): Long = groupSendReport?.rounds?.firstOrNull() ?: 0

    override fun getTimestampMs(): Long = groupSendReport?.timestamp ?: 0

    override fun getMessageID(): ByteArray = groupSendReport?.messageId ?: byteArrayOf()

    override fun getRoundUrl(): String? = groupSendReport?.roundUrl
}