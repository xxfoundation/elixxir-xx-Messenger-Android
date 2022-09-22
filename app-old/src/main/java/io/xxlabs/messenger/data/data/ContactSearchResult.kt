package io.xxlabs.messenger.data.data

import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase

data class ContactSearchResult(
    val contactWrapper: ContactWrapperBase? = null,
    val error: String = "",
    val searchedFact: String = ""
) {
    fun hasError(): Boolean {
        return error.isNotEmpty()
    }

    fun hasContact(): Boolean {
        return contactWrapper != null
    }
}