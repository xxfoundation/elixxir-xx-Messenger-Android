package io.xxlabs.messenger.bindings.wrapper.groups.report

interface GroupSendReportBase {
    fun getRoundID(): Long
    fun getTimestampMs(): Long
    fun getMessageID(): ByteArray
    fun getRoundUrl(): String?
}