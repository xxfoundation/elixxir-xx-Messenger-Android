package io.xxlabs.messenger.requests.ui.details.contact

import io.xxlabs.messenger.requests.model.ContactRequest

interface RequestDetailsListener {
    fun acceptRequest(request: ContactRequest, nickname: String?)
    fun hideRequest(request: ContactRequest)
}