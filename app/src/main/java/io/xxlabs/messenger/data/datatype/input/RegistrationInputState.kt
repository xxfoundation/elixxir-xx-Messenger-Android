package io.xxlabs.messenger.data.datatype.input

enum class RegistrationInputState {
    NONE,
    NAME_MESSAGE,
    EMPTY,
    NAME_INVALID_LENGTH,
    NAME_INVALID_CHARACTERS,
    NAME_MAX_CHARACTERS,
    NAME_VALID,
    REGISTRATION_ACCOUNT_CREATED
}