package io.xxlabs.messenger.bindings.wrapper.round

import io.elixxir.xxclient.utils.RoundId


class RoundListBindings(private val list: List<RoundId>?): RoundListBase {
    override fun get(index: Long): Long {
        return list?.getOrNull(index.toInt()) ?: 0
    }

    override fun len(): Long {
        return list?.size?.toLong() ?: 0
    }

    override fun toList(): List<Long> {
        return list ?: listOf()
    }
}