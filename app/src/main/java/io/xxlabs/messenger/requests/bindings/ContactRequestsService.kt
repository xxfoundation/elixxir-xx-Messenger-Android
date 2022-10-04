package io.xxlabs.messenger.requests.bindings

import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.requests.model.ContactRequest

interface ContactRequestsService {
    suspend fun acceptContactRequest(request: ContactRequest): Boolean
    suspend fun sendContactRequest(request: ContactRequest): Boolean
    suspend fun verifyContactRequest(request: ContactRequest): VerificationResult
    suspend fun deleteContactRequest(request: ContactRequest): Boolean
    fun resetSession(contact: Contact): Boolean
}