package io.xxlabs.messenger.requests.data

/**
 * Encapsulates SharedPreferences related to contact requests and group invitations.
 */
interface RequestPreferencesRepository {
    var requestsMigrated: Boolean
    var invitationsMigrated: Boolean

    @Deprecated(
        "Use a RequestDataSource<ContactRequest>",
        replaceWith = ReplaceWith(
            "ContactRequestsRepository.save()",
            imports = ["io.xxlabs.messenger.requests.data.contact.ContactRequestsRepository"]
        )
    )
    fun addContactRequest(contactId: ByteArray, contactUsername: String, roundId: Long, isSent: Boolean)

    @Deprecated(
        "Use a RequestDataSource<ContactRequest>",
        replaceWith = ReplaceWith(
            "ContactRequestsRepository.save()",
            imports = ["io.xxlabs.messenger.requests.data.contact.ContactRequestsRepository"]
        )
    )
    fun removeContactRequests(contactId: ByteArray): Int
}