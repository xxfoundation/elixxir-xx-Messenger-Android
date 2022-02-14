package io.xxlabs.messenger.bindings.wrapper.round

interface RoundListBase {
    fun get(l: Long)
    fun len(): Long
    fun toList(): List<Long>
}