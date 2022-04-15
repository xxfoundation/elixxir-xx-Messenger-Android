package io.xxlabs.messenger.data.datatype

enum class RequestStatus(val value: Int) {
    REJECTED(-1),
    SENT(0),
    RECEIVED(1),
    ACCEPTED(2),
    SEND_FAIL(3),
    CONFIRM_FAIL(4),
    UNVERIFIED(5),
    VERIFYING(6),
    RESET_SENT(7),
    RESET_FAIL(8);

    companion object {
        fun from(value: Int) = values().first { it.value == value }
    }
}