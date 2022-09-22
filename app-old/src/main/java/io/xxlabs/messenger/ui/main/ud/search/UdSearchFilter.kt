package io.xxlabs.messenger.ui.main.ud.search

enum class UdSearchFilter(val value: String) {
    USERNAME("Username"),
    EMAIL("Email"),
    PHONE("Phone");

    companion object {
        fun from(value: String) = values().first { it.value == value }
    }
}