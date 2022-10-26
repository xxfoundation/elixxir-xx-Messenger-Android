package io.elixxir.xxmessengerclient.utils

import io.elixxir.xxclient.callbacks.AuthEventListener

class AuthCallbacksRegistry {
    private var authCallback: AuthEventListener? = null
    private var authEventCache: AuthEventCache? = null

    fun getAuthCallback(): AuthEventListener {
        return authCallback ?: authEventCache ?: run {
            AuthEventCache().apply {
                authEventCache = this
            }
        }
    }

    fun setAuthCallback(authEventListener: AuthEventListener) {
        authEventCache?.setListener(authEventListener) ?: run {
            authCallback = authEventListener
        }
    }
}

private data class AuthEvent(
    val contact: ByteArray?,
    val receptionId: ByteArray?,
    val ephemeralId: Long,
    val roundId: Long
)

private class AuthEventCache : AuthEventListener {

    private var listener: AuthEventListener? = null

    private val confirmsCache = mutableListOf<AuthEvent>()
    private val requestsCache = mutableListOf<AuthEvent>()
    private val resetsCache = mutableListOf<AuthEvent>()

    fun setListener(listener: AuthEventListener) {
        this.listener = listener
        sendEvents(listener)
        clearCaches()
    }

    private fun clearCaches() {
        confirmsCache.clear()
        requestsCache.clear()
        resetsCache.clear()
    }

    private fun sendEvents(listener: AuthEventListener) {
        confirmsCache.forEach {
            listener.onConfirm(
                it.contact, it.receptionId, it.ephemeralId, it.roundId
            )
        }

        requestsCache.forEach {
            listener.onRequest(
                it.contact, it.receptionId, it.ephemeralId, it.roundId
            )
        }

        resetsCache.forEach {
            listener.onReset(
                it.contact, it.receptionId, it.ephemeralId, it.roundId
            )
        }
    }

    override fun onConfirm(
        contact: ByteArray?,
        receptionId: ByteArray?,
        ephemeralId: Long,
        roundId: Long
    ) {
        listener?.onConfirm(contact, receptionId, ephemeralId, roundId) ?: run {
            confirmsCache.add(
                AuthEvent(contact, receptionId, ephemeralId, roundId)
            )
        }
    }

    override fun onRequest(
        contact: ByteArray?,
        receptionId: ByteArray?,
        ephemeralId: Long,
        roundId: Long
    ) {
        listener?.onRequest(contact, receptionId, ephemeralId, roundId) ?: run {
            requestsCache.add(
                AuthEvent(contact, receptionId, ephemeralId, roundId)
            )
        }
    }

    override fun onReset(
        contact: ByteArray?,
        receptionId: ByteArray?,
        ephemeralId: Long,
        roundId: Long
    ) {
        listener?.onReset(contact, receptionId, ephemeralId, roundId) ?: run {
            resetsCache.add(
                AuthEvent(contact, receptionId, ephemeralId, roundId)
            )
        }
    }
}