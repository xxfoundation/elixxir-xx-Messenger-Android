package io.xxlabs.messenger.bindings.wrapper.groups.report

import bindings.NewGroupReport
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBindings
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBindings

class NewGroupReportBindings(val newGroupReport: NewGroupReport) : NewGroupReportBase{
    override fun getGroup(): GroupBindings {
        return GroupBindings(newGroupReport.group)
    }

    override fun getRoundList(): RoundListBindings {
        return RoundListBindings(newGroupReport.roundList)
    }

    override fun getStatus(): Long {
        return newGroupReport.status
    }

    override fun marshal(): ByteArray {
        return newGroupReport.marshal()
    }
}