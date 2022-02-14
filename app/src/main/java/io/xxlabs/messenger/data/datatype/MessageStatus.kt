package io.xxlabs.messenger.data.datatype

enum class MessageStatus(val value: Int) {
    PENDING(0),
    SENT(1),
    RECEIVED(2),
    FAILED(3);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}