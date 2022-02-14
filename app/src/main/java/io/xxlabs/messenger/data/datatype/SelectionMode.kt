package io.xxlabs.messenger.data.datatype

enum class SelectionMode(val value: Int) {
    CONTACT_ACCESS(0),
    PROFILE_ACCESS(1),
    CHAT_ACCESS(2),
    CHAT_SELECTION(3),
    GROUP_ACCESS(4)
}