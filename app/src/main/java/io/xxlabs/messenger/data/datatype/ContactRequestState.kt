package io.xxlabs.messenger.data.datatype

enum class ContactRequestState(val value: Int) {
    VERIFYING(0),
    SUCCESS(1),
    FAILED(-1),
    RECEIVED(2);

    companion object {
        fun from(value: Int) = values().first { it.value == value }
    }
}