package io.xxlabs.messenger.requests.bindings

import android.util.Pair
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.requests.bindings.VerificationResult.*
import io.xxlabs.messenger.requests.model.ContactRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface RequestVerifier {
    suspend fun verifyRequest(request: ContactRequest): VerificationResult
}

sealed class VerificationResult {
    abstract val receivedIdentity: Contact
    abstract val bindingsIdentity: ContactWrapperBase?

    data class Verified(
        override val receivedIdentity: Contact,
        override val bindingsIdentity: ContactWrapperBase? = null
    ) : VerificationResult()

    data class Fraudulent(
        override val receivedIdentity: Contact,
        override val bindingsIdentity: ContactWrapperBase? = null
    ) : VerificationResult()

    data class Verifying(
        override val receivedIdentity: Contact,
        override val bindingsIdentity: ContactWrapperBase? = null
    ) : VerificationResult()

    data class Unverified(
        override val receivedIdentity: Contact,
        override val bindingsIdentity: ContactWrapperBase? = null
    ) : VerificationResult()
}

class BindingsRequestVerifier @Inject constructor(
    private val repo: BaseRepository
) : RequestVerifier {

    override suspend fun verifyRequest(request: ContactRequest): VerificationResult =
        withContext(Dispatchers.IO) {
            val verificationData = with (request.model) {
                if (hasFacts()) lookupFacts(this)
                else lookupUserId(this)
            }
            when (verificationData) {
                is Verifying -> verifyOwnership(verificationData)
                else ->  Unverified(verificationData.receivedIdentity)
            }
        }

    private suspend fun lookupFacts(
        user: Contact
    ): VerificationResult = suspendCoroutine { continuation ->
        val factPair: Pair<String, FactType> = when {
            user.phone.isNotBlank() -> Pair(user.phone, FactType.PHONE)
            user.email.isNotBlank() -> Pair(user.email, FactType.EMAIL)
            else -> Pair("not found", FactType.NICKNAME).also {
                continuation.resume(Unverified(user))
            }
        }

        repo.searchUd(factPair.first, factPair.second) { result, error ->
            error?.let {
                if (it.isBlank()) continuation.resume(Unverified(user))
                return@searchUd
            }

            result?.let { contactBindings ->
                if (contactBindings.getId().contentEquals(user.userId)) {
                    continuation.resume(Verifying(user, contactBindings))
                } else {
                    continuation.resume(Fraudulent(user))
                }
            } ?: run {
                continuation.resume(Fraudulent(user))
            }
        }
    }

    private suspend fun lookupUserId(
        user: Contact
    ): VerificationResult = suspendCoroutine { continuation ->
        repo.userLookup(user.userId) { result, error ->
            if (!error.isNullOrEmpty()) {
                continuation.resume(Unverified(user))
                return@userLookup
            }

            result?.let { contactBindings ->
                if (contactBindings.getId().contentEquals(user.userId)) {
                    continuation.resume(Verifying(user, contactBindings))
                } else {
                    continuation.resume(Fraudulent(user))
                }
            }
        }
    }

    private suspend fun verifyOwnership(
        verification: Verifying
    ): VerificationResult = suspendCoroutine { continuation ->
        verification.bindingsIdentity?.let { bindingsData ->
            if (repo.verifyOwnership(verification.receivedIdentity, bindingsData)) {
                continuation.resume(
                    Verified(
                        verification.receivedIdentity,
                        bindingsData
                    )
                )
            } else {
                continuation.resume(Fraudulent(verification.receivedIdentity))
            }
        }

    }
}