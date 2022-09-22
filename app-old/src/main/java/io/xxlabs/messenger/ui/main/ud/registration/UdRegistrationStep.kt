package io.xxlabs.messenger.ui.main.ud.registration

enum class UdRegistrationStep(val value: Int) {
    NONE(0),
    EMAIL_INPUT_SUCCESS(1),
    PHONE_INPUT_SUCCESS(2),
    ALL_DONE(3);

    companion object {
        fun from(value: Int) = values().first { it.value == value }
    }
}