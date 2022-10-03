package io.xxlabs.messenger.requests.bindings

import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.repository.client.ClientRepository
import io.xxlabs.messenger.requests.model.ContactRequest
import io.xxlabs.messenger.support.util.value
import timber.log.Timber
import javax.inject.Inject

class BindingsRequestMediator @Inject constructor(
    private val repo: BaseRepository,
    private val requestVerifier: RequestVerifier
): ContactRequestsService {

    override suspend fun acceptContactRequest(request: ContactRequest): Boolean =
        confirmAuthenticatedChannel(request.model)

    private suspend fun confirmAuthenticatedChannel(contact: Contact): Boolean {
        var result = false
        unmarshalContact(contact)?.let { bindingsData ->
            try {
                repo.confirmAuthenticatedChannel(bindingsData).value()
                result = true
            } catch (e: Exception) {
                val logMsg =
                    "Exception occurred when accepting the " +
                    "request from ${contact.displayName}: ${e.message}."
                Timber.d(logMsg)
            }
        }
        return result
    }

    private fun unmarshalContact(contact: Contact): ByteArray? {
        var contactWrapper: ContactWrapperBase? = null

        contact.marshaled?.let { marshalled ->
            contactWrapper = repo.unmarshallContact(marshalled)
        }
        return contactWrapper?.marshal()
    }

    override suspend fun sendContactRequest(request: ContactRequest): Boolean =
        requestAuthenticatedChannel(request.model)

    private suspend fun requestAuthenticatedChannel(contact: Contact): Boolean {
        var result = false
        unmarshalContact(contact)?.let { bindingsData ->
            try {
                repo.requestAuthenticatedChannel(bindingsData).value()
                result = true
            } catch (e: Exception) {
                val logMsg =
                    "Exception occurred when sending the " +
                    "request to ${contact.displayName}: ${e.message}."
                Timber.d(logMsg)
            }
        }
        return result
    }

    override suspend fun verifyContactRequest(request: ContactRequest): VerificationResult =
       requestVerifier.verifyRequest(request)

    override fun resetSession(contact: Contact): Boolean {
        TODO()
//        var result = false
//        try {
//            val roundId = ClientRepository.clientWrapper.client.resetSession(
//                contact.marshaled,
//                repo.getMashalledUser(),
//                ""
//            )
//            result = roundId > 0
//        } catch (e: Exception) {
//            Timber.d(
//                "Exception occurred when resetting ${contact.displayName}: ${e.message}."
//            )
//        }
//        return result
    }
}