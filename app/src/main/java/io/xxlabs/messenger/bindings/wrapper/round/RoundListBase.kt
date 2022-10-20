package io.xxlabs.messenger.bindings.wrapper.round

interface RoundListBase {
    fun get(index: Long): Long
    fun len(): Long
    fun toList(): List<Long>
}