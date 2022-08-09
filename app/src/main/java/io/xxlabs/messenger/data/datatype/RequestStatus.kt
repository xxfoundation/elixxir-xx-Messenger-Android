package io.xxlabs.messenger.data.datatype

enum class RequestStatus(val value: Int) {
    SENT(0),
    VERIFIED(1),
    ACCEPTED(2),
    SEND_FAIL(3),
    CONFIRM_FAIL(4),
    VERIFICATION_FAIL(5),
    VERIFYING(6),
    RESET_SENT(9),
    RESET_FAIL(8),
    RESENT(7),
    SENDING(10),
    DELETING(11),
    HIDDEN(12),
    SEARCH(99);

    companion object {
        fun from(value: Int) = values().first { it.value == value }
    }
}