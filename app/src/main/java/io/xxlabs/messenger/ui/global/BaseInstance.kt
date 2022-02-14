package io.xxlabs.messenger.ui.global

interface BaseInstance {
    fun activeInstancesCount(): Int
    fun isActive(): Boolean
}