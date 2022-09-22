package io.xxlabs.messenger.data.datatype

enum class NetworkFollowerStatus(val value: Long) {
    STOPPED(0),
    STARTING(1000),
    RUNNING(2000),
    STOPPING(3000);

    companion object {
        fun from(value: Long) = values().first { it.value == value }
    }
}