package io.xxlabs.messenger.bindings.wrapper.groups.report

import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupMock
import io.xxlabs.messenger.bindings.wrapper.round.RoundListMock
import io.xxlabs.messenger.data.room.model.GroupData

class NewGroupReportMock(val group: GroupData, val idsList: List<ByteArray>) : NewGroupReportBase{
    override fun getGroup(): GroupBase? {
        return GroupMock(group, idsList)
    }

    override fun getRoundList(): RoundListMock {
        return RoundListMock()
    }

    override fun getStatus(): Long {
        return 3L
    }

    override fun marshal(): ByteArray {
        return this.toString().encodeToByteArray()
    }

    override fun toString(): String {
        return "$group||$idsList"
    }
}