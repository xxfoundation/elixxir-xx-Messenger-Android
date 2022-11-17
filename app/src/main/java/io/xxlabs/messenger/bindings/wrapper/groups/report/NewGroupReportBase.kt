package io.xxlabs.messenger.bindings.wrapper.groups.report

import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase

interface NewGroupReportBase {
    fun getGroup(): GroupBase?
    fun getRoundList(): RoundListBase
    fun getStatus(): Long
    fun marshal(): ByteArray
}