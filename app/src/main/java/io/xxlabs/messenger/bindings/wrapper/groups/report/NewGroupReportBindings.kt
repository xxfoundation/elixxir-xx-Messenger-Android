package io.xxlabs.messenger.bindings.wrapper.groups.report

import io.elixxir.xxclient.group.Group
import io.elixxir.xxclient.models.BindingsModel.Companion.encode
import io.elixxir.xxclient.models.GroupReport
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBindings
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBindings

class NewGroupReportBindings(
    private val newGroupReport: GroupReport?,
    private val group: Group?
) : NewGroupReportBase {
    override fun getGroup(): GroupBindings {
        return GroupBindings(group!!)
    }

    override fun getRoundList(): RoundListBindings {
        return RoundListBindings(newGroupReport!!.rounds)
    }

    override fun getStatus(): Long {
        return newGroupReport!!.status
    }

    override fun marshal(): ByteArray {
        return newGroupReport?.let { encode(it) } ?: byteArrayOf()
    }
}