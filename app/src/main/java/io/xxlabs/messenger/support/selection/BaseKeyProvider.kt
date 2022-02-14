package io.xxlabs.messenger.support.selection

interface BaseKeyProvider<K> {
    fun getKey(position: Int): K
    fun getPosition(key: K): Int
}