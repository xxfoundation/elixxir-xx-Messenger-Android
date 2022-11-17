package io.xxlabs.messenger.bindings.wrapper.groups.report

import io.elixxir.xxclient.group.Group
import io.elixxir.xxclient.models.BindingsModel.Companion.encode
import io.elixxir.xxclient.models.GroupReport
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBindings
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBindings

class NewGroupReportBindings(
    private val newGroupReport: GroupReport?,
    private val group: Group?
) : NewGroupReportBase {
    override fun getGroup(): GroupBase? = group?.let { GroupBindings(it) }

    override fun getRoundList(): RoundListBindings = RoundListBindings(
    newGroupReport?.rounds?.takeUnless { it.isEmpty() } ?: listOf()
    )

    override fun getStatus(): Long = newGroupReport?.status ?: 0

    override fun marshal(): ByteArray =
        newGroupReport?.let { encode(it) } ?: byteArrayOf()
}