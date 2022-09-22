package io.xxlabs.messenger.bindings.wrapper.groups.report

import io.xxlabs.messenger.support.util.Utils

class GroupSendReportMock : GroupSendReportBase {
    override fun getRoundID(): Long {
        return 1
    }

    override fun getTimestampMs(): Long {
        return Utils.getCurrentTimeStamp()
    }

    override fun getMessageID(): ByteArray = byteArrayOf()

    override fun getRoundUrl(): String = "https://google.com"
}