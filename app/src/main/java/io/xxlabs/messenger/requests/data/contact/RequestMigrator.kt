package io.xxlabs.messenger.requests.data.contact

import io.xxlabs.messenger.data.data.ContactRoundRequest
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.util.value
import kotlinx.coroutines.*
import timber.log.Timber

object RequestMigrator {

    /**
     * Moves requests previously saved to [PreferencesRepository] as
     * [ContactRoundRequest]s to the [ContactRequestsRepository] as
     * [ContactRequestData].
     */
    suspend fun performMigration(
        preferences: PreferencesRepository,
        requestsDataSource: ContactRequestsRepository,
        daoRepository: DaoRepository
    ) = withContext(Dispatchers.IO) {
        val requestsToMigrate = ContactRoundRequest.toRoundRequestsSet(
            preferences.contactRoundRequests
        )
        for (oldRequest in requestsToMigrate) {
            launch {
                val contact = getContact(oldRequest, daoRepository)
                contact?.let {
                    requestsDataSource.save(ContactRequestData(contact, false))
                    preferences.removeRequest(oldRequest)
                }
            }
        }
    }

    private suspend fun getContact(
        oldRequest: ContactRoundRequest,
        daoRepository: DaoRepository
    ): Contact? {
        return try {
            daoRepository.getContactByUserId(oldRequest.contactId).value()
        } catch (e: Exception) {
            Timber.d("An error occured during request migration: ${e.message}")
            null
        }
    }

    private fun PreferencesRepository.removeRequest(oldRequest: ContactRoundRequest) {
        val numRemoved = removeContactRequests(oldRequest.contactId)
        if (contactsCount >= numRemoved) contactsCount -= numRemoved
    }
}