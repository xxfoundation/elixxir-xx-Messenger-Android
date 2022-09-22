package io.xxlabs.messenger.bindings.wrapper.report

import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase

interface SendReportBase {
    fun getMessageId(): ByteArray
    fun getRoundList(): RoundListBase
    fun getTimestampMs(): Long
    fun getTimestampNano(): Long
    fun marshal(): ByteArray
    fun getRoundUrl(): String
}