package io.xxlabs.messenger.bindings.wrapper.groups.report

import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBindings
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBindings

interface NewGroupReportBase {
    fun getGroup(): GroupBase
    fun getRoundList(): RoundListBase
    fun getStatus(): Long
    fun marshal(): ByteArray
}