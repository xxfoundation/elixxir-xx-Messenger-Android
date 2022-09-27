package io.elixxir.data.networking.data

import io.elixxir.core.logging.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val MAX_NETWORK_RETRIES = 29
private const val NETWORK_POLL_INTERVAL_MS = 1000L


class NetworkDataSource {

    private suspend fun connectToCmix(retries: Int = 0) {
        networking.checkRegisterNetworkCallback()
        if (retries < MAX_NETWORK_RETRIES) {
            if (initializeNetworkFollower()) {
                log("Started network follower after #${retries + 1} attempt(s).")
                withContext(Dispatchers.Main) {
                    onUsernameNextClicked()
                }
            } else {
                delay(NETWORK_POLL_INTERVAL_MS)
                log("Attempting to start network follower, attempt #${retries + 1}.")
                connectToCmix(retries + 1)
            }
        } else throw Exception("Failed to connect to network after ${retries + 1} attempts. Please try again.")
    }

    private suspend fun initializeNetworkFollower(): Boolean = suspendCoroutine { continuation ->
        networking.tryStartNetworkFollower { successful ->
            continuation.resume(successful)
        }
    }

}