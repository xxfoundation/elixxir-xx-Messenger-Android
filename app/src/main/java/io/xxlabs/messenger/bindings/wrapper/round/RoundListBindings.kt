package io.xxlabs.messenger.bindings.wrapper.round

import bindings.RoundList

class RoundListBindings(val roundList: RoundList): RoundListBase {
    override fun get(l: Long) {
        roundList[l]
    }

    override fun len(): Long {
        return roundList.len()
    }

    override fun toList(): List<Long> {
        val list = mutableListOf<Long>()
        for (i in 0 until len()) {
            list.add(roundList[i])
        }
        return list
    }
}