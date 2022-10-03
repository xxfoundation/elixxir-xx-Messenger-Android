package io.xxlabs.messenger.bindings.wrapper.groups.report

import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBindings
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBindings

class NewGroupReportBindings(/*val newGroupReport: NewGroupReport*/) : NewGroupReportBase{
    override fun getGroup(): GroupBindings {
        TODO()
//        return GroupBindings(newGroupReport.group)
    }

    override fun getRoundList(): RoundListBindings {
        TODO()
//        return RoundListBindings(newGroupReport.roundList)
    }

    override fun getStatus(): Long {
        TODO()
//        return newGroupReport.status
    }

    override fun marshal(): ByteArray {
        TODO()
//        return newGroupReport.marshal()
    }
}